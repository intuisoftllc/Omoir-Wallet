package com.intuisoft.plaid.features.dashboardflow.pro.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.model.*
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.nextInt
import com.intuisoft.plaid.common.util.extensions.splitIntoGroupOf
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.util.Plural
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.AtpManager
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.*
import kotlin.math.min


class AtpViewModel(
    application: Application,
    private val apiRepository: ApiRepository,
    private val localStoreRepository: LocalStoreRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _displayRecipient = SingleLiveData<LocalWalletModel>()
    val displayRecipient: LiveData<LocalWalletModel> = _displayRecipient

    protected val _noWallets = SingleLiveData<Unit>()
    val noWallets: LiveData<Unit> = _noWallets

    protected val _maxSpend = SingleLiveData<String>()
    val maxSpend: LiveData<String> = _maxSpend

    protected val _batchGap = SingleLiveData<String>()
    val batchGap: LiveData<String> = _batchGap

    protected val _batchSize = SingleLiveData<String>()
    val batchSize: LiveData<String> = _batchSize

    protected val _feeSpread = SingleLiveData<String>()
    val feeSpread: LiveData<String> = _feeSpread

    protected val _nextEnabled = SingleLiveData<Boolean>()
    val nextEnabled: LiveData<Boolean> = _nextEnabled

    protected val _confirmTransfer = SingleLiveData<TransferData>()
    val confirmTransfer: LiveData<TransferData> = _confirmTransfer

    protected val _onDisplayExplanation = SingleLiveData<String>()
    val onDisplayExplanation: LiveData<String> = _onDisplayExplanation

    protected val _creatingTransfer = SingleLiveData<Unit>()
    val creatingTransfer: LiveData<Unit> = _creatingTransfer

    protected val _transferCreated = SingleLiveData<String>()
    val transferCreated: LiveData<String> = _transferCreated

    protected val _contentNotAvailable = SingleLiveData<Unit>()
    val contentNotAvailable: LiveData<Unit> = _contentNotAvailable

    private var recipient: LocalWalletModel? = null
    val randomNumberGenerator = SecureRandom()

    fun setWallet(wallet: LocalWalletModel) {
        recipient = wallet
        _displayRecipient.postValue(wallet)
    }

    fun getInitialWallet() {
        if(isReadOnly()) {
            _contentNotAvailable.postValue(Unit)
        } else {
            recipient = walletManager.getWallets().filter {
                it.uuid != getWalletId() && it.testNetWallet == isTestNetWallet()
            }.firstOrNull()


            if (recipient == null) {
                _noWallets.postValue(Unit)
                _nextEnabled.postValue(false)
            } else {
                setWallet(
                    recipient!!
                )
            }
        }
    }

    fun updateValues() {
        if(isReadOnly()) {
            _contentNotAvailable.postValue(Unit)
        } else {
            updateMaxSpend()
            updateBatchGap()
            updateBatchSize()
            updateFeeSpread()
        }
    }

    fun updateBatchGap() {
        _batchGap.postValue(Plural.of("Block", localStoreRepository.getBatchGap().toLong()))
    }

    fun updateBatchSize() {
        _batchSize.postValue(Plural.of("Utxo", localStoreRepository.getBatchSize().toLong()))
    }

    fun updateFeeSpread() {
        val spread = localStoreRepository.getFeeSpread()

        if((spread.first == 0 && spread.last == 0) || localStoreRepository.isUsingDynamicBatchNetworkFee()) {
            _feeSpread.postValue(getApplication<PlaidApp>().getString(R.string.dynamic_network_fee))
        } else {
            _feeSpread.postValue(getApplication<PlaidApp>().getString(R.string.sat_per_vbyte, "${spread.first}-${spread.last}"))
        }
    }

    fun createTransfer(data: TransferData) {
        viewModelScope.launch {
            _creatingTransfer.postValue(Unit)
            val transferId = UUID.randomUUID().toString()
            val batches = mutableListOf<String>()

            data.utxos.splitIntoGroupOf(data.batchSize)
                .forEachIndexed { index, batch ->
                    batches.add(UUID.randomUUID().toString())

                    localStoreRepository.setBatchData(
                        BatchDataModel(
                            id = batches.last(),
                            transferId = transferId,
                            batchNumber = index,
                            completionHeight = 0,
                            blocksRemaining = 0,
                            utxos = batch.items.map {
                                UtxoTransfer(
                                    txId = "",
                                    address = it.utxo.output.address!!,
                                    feeRate = it.feeRate
                                )
                            },
                            status = AssetTransferStatus.NOT_STARTED
                        )
                    )
                }

            localStoreRepository.saveAssetTransfer(
                AssetTransferModel(
                    id = transferId,
                    walletId = getWalletId(),
                    recipientWallet = data.to.uuid,
                    createdAt = System.currentTimeMillis(),
                    batchGap = data.batchGap + data.batchPenalty,
                    batchSize = data.batchSize,
                    expectedAmount = data.sendAmount,
                    sent = 0,
                    feesPaid = 0,
                    retries = 0,
                    feeRangeLow = getFeeSpread().first,
                    feeRangeHigh = getFeeSpread().last,
                    dynamicFees = isUsingDynamicBatchNetworkFee(),
                    status = AssetTransferStatus.NOT_STARTED,
                    batches = batches
                )
            )

            data.utxos.forEach {
                localStoreRepository.blacklistAddress(
                    address = BlacklistedAddressModel(
                        address = it.utxo.output.address!!
                    ),
                    blacklist = true
                )
            }

            updateUTXOs(mutableListOf())
            updateValues()
            _transferCreated.postValue(transferId)
        }
    }

    fun confirmAssetTransfer() {
        viewModelScope.launch {
            _nextEnabled.postValue(false)
            _loading.postValue(true)

            if(NetworkUtil.hasInternet(getApplication())) {
                if (selectedUTXOs.isEmpty()) {
                    selectedUTXOs = getUnspentOutputs().toMutableList()
                }

                if(selectedUTXOs.isNotEmpty()) {
                    val batchGap = getBatchGap()
                    val batchSize = min(getBatchSize(), selectedUTXOs.size)
                    var penalty = batchSize / Constants.Limit.BATCH_PENALTY_THRESHOLD
                    val randomFees = selectedUTXOs.map { randomNumberGenerator.nextInt(getFeeSpread().first, getFeeSpread().last) }
                    val batchesNeeded = selectedUTXOs.splitIntoGroupOf(batchSize).size
                    var blocksNeeded =
                        if(batchesNeeded == 1) batchesNeeded
                        else batchesNeeded + (batchGap * (batchesNeeded - 1))

                    if(batchesNeeded > 1) {
                        blocksNeeded += (penalty * ((batchesNeeded - 1)))
                    } else {
                        penalty = 0
                    }

                    if (isUsingDynamicBatchNetworkFee()) {
                        var baseFee =
                            localStoreRepository.getSuggestedFeeRate(isTestNetWallet())?.highFee ?: 0
                        val estimatedTime = blocksNeeded * Constants.Time.BLOCK_TIME.toLong()
                        val estimatedFees = selectedUTXOs.mapIndexed { index, it ->
                            var result = AtpManager.calculateFeeForMaxSpend(getWallet()!!, it, baseFee + randomFees[index], null)

                            if(result.first <= 0) { // use lowest possible fee
                                AtpManager.calculateFeeForMaxSpend(getWallet()!!, it, baseFee + getFeeSpread().first, null).first to baseFee + getFeeSpread().first
                            } else result.first to baseFee + randomFees[index]
                        }

                        val estimatedFee = estimatedFees.filter { it.first > 0 }.map { it.first }.sum()
                        val utxos = selectedUTXOs.mapIndexed { index, it ->
                            UtxoData(
                                utxo = it,
                                totalFee = estimatedFees[index].first,
                                feeRate = estimatedFees[index].second
                            )
                        }
                        val sendAmount = utxos.sumOf {
                            if(it.totalFee > 0) {
                                it.utxo.output.value - it.totalFee
                            } else 0
                        }

                        if(sendAmount > 0) {
                            _confirmTransfer.postValue(
                                TransferData(
                                    to = recipient!!,
                                    batchGap = batchGap,
                                    batchSize = batchSize,
                                    batchPenalty = penalty,
                                    estimatedCompletionTime = estimatedTime,
                                    estimatedFee = estimatedFee,
                                    sendAmount = utxos.sumOf {
                                        if(it.totalFee > 0) {
                                            it.utxo.output.value - it.totalFee
                                        } else 0
                                    },
                                    utxos = utxos
                                )
                            )
                        } else {
                            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.atp_confirm_error_not_enough_funds))
                        }
                    } else {
                        val estimatedFees = selectedUTXOs.mapIndexed { index, it ->
                            var result = AtpManager.calculateFeeForMaxSpend(getWallet()!!, it, randomFees[index], null)
                            var rate = randomFees[index]

                            if(result.first <= 0) { // use lowest possible fee
                                result = AtpManager.calculateFeeForMaxSpend(getWallet()!!, it, getFeeSpread().first, null)
                                rate = getFeeSpread().first
                            }

                            result.first to rate
                        }

                        val estimatedTime = blocksNeeded * Constants.Time.BLOCK_TIME.toLong()
                        val estimatedFee = estimatedFees.filter { it.first > 0 }.map { it.first }.sum()
                        val utxos = selectedUTXOs.mapIndexed { index, it ->
                            UtxoData(
                                utxo = it,
                                totalFee = estimatedFees[index].first,
                                feeRate = estimatedFees[index].second
                            )
                        }
                        val sendAmount = utxos.sumOf {
                            if(it.totalFee > 0) {
                                it.utxo.output.value - it.totalFee
                            } else 0
                        }

                        if(sendAmount > 0) {
                            _confirmTransfer.postValue(
                                TransferData(
                                    to = recipient!!,
                                    batchGap = batchGap,
                                    batchSize = batchSize,
                                    batchPenalty = penalty,
                                    estimatedCompletionTime = estimatedTime,
                                    estimatedFee = estimatedFee,
                                    sendAmount = utxos.sumOf {
                                        if(it.totalFee > 0) {
                                            it.utxo.output.value - it.totalFee
                                        } else 0
                                    },
                                    utxos = utxos
                                )
                            )
                        } else {
                            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.atp_confirm_error_not_enough_funds))
                        }
                    }

                    _nextEnabled.postValue(true)
                } else {
                    _nextEnabled.postValue(true)
                    _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.atp_confirm_error_not_enough_funds_no_utxo))
                }
            } else {
                _nextEnabled.postValue(true)
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.no_internet_connection))
            }

            _loading.postValue(false)
        }
    }

    fun setBatchGap(gap: Int) {
        localStoreRepository.setBatchGap(gap)
        updateBatchGap()
    }

    fun setBatchSize(size: Int) {
        localStoreRepository.setBatchSize(size)
        updateBatchSize()
    }

    fun getBatchGap() = localStoreRepository.getBatchGap()

    fun getBatchSize() = localStoreRepository.getBatchSize()

    fun getFeeSpread() = localStoreRepository.getFeeSpread()

    fun setFeeSpread(range: IntRange) = localStoreRepository.setFeeSpread(range)

    fun isUsingDynamicBatchNetworkFee() = localStoreRepository.isUsingDynamicBatchNetworkFee()

    fun setUsingDynamicBatchNetworkFee(dynamic: Boolean) = localStoreRepository.setUseDynamicBatchNetworkFee(dynamic)

    fun updateMaxSpend() {
        _maxSpend.postValue(getMaxSpend().from(getDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency()).second!!)
    }

    override fun updateUTXOs(utxos: MutableList<UnspentOutput>) {
        super.updateUTXOs(utxos)
        updateMaxSpend()
    }

    override fun addSingleUTXO(utxo: UnspentOutput) {
        super.addSingleUTXO(utxo)
        updateMaxSpend()
    }

    data class TransferData(
        val to: LocalWalletModel,
        val batchGap: Int,
        val batchSize: Int,
        val batchPenalty: Int,
        val estimatedCompletionTime: Long, // in seconds
        val estimatedFee: Long, // in seconds
        val sendAmount: Long, // in seconds
        val utxos: List<UtxoData>
    )

    data class UtxoData(
        val utxo: UnspentOutput,
        val totalFee: Long,
        val feeRate: Int
    )
}