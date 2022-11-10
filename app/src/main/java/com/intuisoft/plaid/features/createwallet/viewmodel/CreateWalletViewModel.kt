package com.intuisoft.plaid.features.createwallet.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SingleLiveData
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.common.repositories.ApiRepository
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.fragmentconfig.WalletConfigurationData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Language
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.horizontalsystems.hdwalletkit.WordList


class CreateWalletViewModel(
    application: Application,
    localStoreRepository: LocalStoreRepository,
    apiRepository: ApiRepository,
    private val walletManager: AbstractWalletManager
): WalletViewModel(application, localStoreRepository, apiRepository, walletManager) {

    protected val _onInputRejected = SingleLiveData<Unit>()
    val onInputRejected: LiveData<Unit> = _onInputRejected

    protected val _onDisplayExplanation = SingleLiveData<String>()
    val onDisplayExplanation: LiveData<String> = _onDisplayExplanation

    protected val _onConfirm = SingleLiveData<Unit>()
    val onConfirm: LiveData<Unit> = _onConfirm

    protected val _onAddWord = SingleLiveData<String>()
    val onAddWord: LiveData<String> = _onAddWord

    protected val _onRemoveLastWord = SingleLiveData<Unit>()
    val onRemoveLastWord: LiveData<Unit> = _onRemoveLastWord

    protected val _importAllowed = SingleLiveData<Boolean>()
    val importAllowed: LiveData<Boolean> = _importAllowed

    protected val _wordSuggestion1 = SingleLiveData<String>()
    val wordSuggestion1: LiveData<String> = _wordSuggestion1

    protected val _wordSuggestion2 = SingleLiveData<String>()
    val wordSuggestion2: LiveData<String> = _wordSuggestion2

    protected val _wordSuggestion3 = SingleLiveData<String>()
    val wordSuggestion3: LiveData<String> = _wordSuggestion3

    protected val _wordSuggestion4 = SingleLiveData<String>()
    val wordSuggestion4: LiveData<String> = _wordSuggestion4

    protected val _wordSuggestionMany = SingleLiveData<Boolean>()
    val wordSuggestionMany: LiveData<Boolean> = _wordSuggestionMany

    protected val _clearWordEntry = SingleLiveData<Unit>()
    val clearWordEntry: LiveData<Unit> = _clearWordEntry

    private var seed: MutableList<String> = mutableListOf()
    private var bip: HDWallet.Purpose = HDWallet.Purpose.BIP84
    private var entropyStrength = Mnemonic.EntropyStrength.Default
    private var pubKey = ""
    private var invalidAddressErrors = 0
    private var invalidSeedWordErrors = 0
    private var maxWordsErrors = 0

    var useTestNet = false
        private set

    fun generateNewWallet() {
        generateNewWallet(entropyStrength)
    }

    fun setUseTestNet(use: Boolean) {
        useTestNet = use
    }

    fun setEntropyStrength(strength: Mnemonic.EntropyStrength) {
        entropyStrength = strength
    }

    fun setLocalBip(bip: HDWallet.Purpose) {
        this.bip = bip
    }

    fun getConfiguration() =
        WalletConfigurationData(
            testNetWallet = useTestNet,
            wordCount = entropyStrength.wordCount,
            bip = bip.ordinal,
            seedPhrase = seed,
            publicKey = pubKey,
        )

    fun setConfiguration(config: WalletConfigurationData) {
        useTestNet = config.testNetWallet
        entropyStrength = Mnemonic.EntropyStrength.fromWordCount(config.wordCount)
        bip = HDWallet.Purpose.values().find { it.ordinal == config.bip } ?: HDWallet.Purpose.BIP84
        seed = config.seedPhrase.toMutableList()
        pubKey = config.publicKey
    }

    fun removeLastWord() {
        if(seed.isNotEmpty()) {
            seed.removeLast()
            checkSeedSize()
            _onRemoveLastWord.postValue(Unit)
        }
    }

    fun importSeedPhrase() {
        try {
            Mnemonic().toSeed(seed)
            _onConfirm.postValue(Unit)
        } catch(err: Throwable) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.import_recovery_phrase_error_invalid_seed_phrase))
        }
    }

    private fun onWordAdded() {
        _clearWordEntry.postValue(Unit)
        _wordSuggestion1.postValue("")
        _wordSuggestion2.postValue("")
        _wordSuggestion3.postValue("")
        _wordSuggestion4.postValue("")
        _wordSuggestionMany.postValue(false)
    }

    fun updateWordSuggestions(word: String) {
        val trimmed = word.trim()

        if(trimmed.isNotEmpty()) {
            val suggestions = WordList.wordList(Language.English).fetchSuggestions(trimmed)

            when {
                suggestions.isEmpty() -> {
                    _wordSuggestion1.postValue("")
                    _wordSuggestion2.postValue("")
                    _wordSuggestion3.postValue("")
                    _wordSuggestion4.postValue("")
                    _wordSuggestionMany.postValue(false)
                }

                suggestions.size == 1 -> {
                    _wordSuggestion1.postValue(suggestions[0])
                    _wordSuggestion2.postValue("")
                    _wordSuggestion3.postValue("")
                    _wordSuggestion4.postValue("")
                    _wordSuggestionMany.postValue(false)
                }

                suggestions.size == 2 -> {
                    _wordSuggestion1.postValue(suggestions[0])
                    _wordSuggestion2.postValue(suggestions[1])
                    _wordSuggestion3.postValue("")
                    _wordSuggestion4.postValue("")
                    _wordSuggestionMany.postValue(false)
                }

                suggestions.size == 3 -> {
                    _wordSuggestion1.postValue(suggestions[0])
                    _wordSuggestion2.postValue(suggestions[1])
                    _wordSuggestion3.postValue(suggestions[2])
                    _wordSuggestion4.postValue("")
                    _wordSuggestionMany.postValue(false)
                }

                suggestions.size == 4 -> {
                    _wordSuggestion1.postValue(suggestions[0])
                    _wordSuggestion2.postValue(suggestions[1])
                    _wordSuggestion3.postValue(suggestions[2])
                    _wordSuggestion4.postValue(suggestions[3])
                    _wordSuggestionMany.postValue(false)
                }

                else -> {
                    _wordSuggestion1.postValue(suggestions[0])
                    _wordSuggestion2.postValue(suggestions[1])
                    _wordSuggestion3.postValue(suggestions[2])
                    _wordSuggestion4.postValue(suggestions[3])
                    _wordSuggestionMany.postValue(true)
                }
            }
        } else {
            _wordSuggestion1.postValue("")
            _wordSuggestion2.postValue("")
            _wordSuggestion3.postValue("")
            _wordSuggestion4.postValue("")
            _wordSuggestionMany.postValue(false)
        }
    }

    private fun checkSeedSize() {
        when(seed.size) {
            Mnemonic.EntropyStrength.Default.wordCount,
            Mnemonic.EntropyStrength.Low.wordCount,
            Mnemonic.EntropyStrength.Medium.wordCount,
            Mnemonic.EntropyStrength.High.wordCount,
            Mnemonic.EntropyStrength.VeryHigh.wordCount -> {
                _importAllowed.postValue(true)
            }
            else -> {
                _importAllowed.postValue(false)
            }
        }
    }

    private fun addWord(word: String) {
        val trimmed = word.trim()

        seed.add(trimmed)
        _onAddWord.postValue(trimmed)
        onWordAdded()
    }

    fun addWordToList(word: String, partialWordSupport: Boolean = false) {
        val trimmed = word.trim()
        if(WordList.wordList(Language.English).validWord(trimmed)) {
            if(seed.size < maxWords) {
                addWord(trimmed)
            } else {
                onMaxWords()
            }

            checkSeedSize()
        } else if(partialWordSupport) {
            val suggestions = WordList.wordList(Language.English).fetchSuggestions(trimmed)
            if(suggestions.size == 1) {
                addWord(suggestions[0])
                checkSeedSize()
            } else {
                onInvalidWord()
            }
        }  else {
            onInvalidWord()
        }
    }

    private fun onInvalidWord() {
        _onInputRejected.postValue(Unit)
        invalidSeedWordErrors++

        if(invalidSeedWordErrors % errorThreshold == 0) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.import_recovery_phrase_error_invalid_seed_word))
        }
    }

    private fun onMaxWords() {
        _onInputRejected.postValue(Unit)
        maxWordsErrors++

        if(maxWordsErrors % errorThreshold == 0) {
            _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.import_recovery_phrase_error_max_words))
        }
    }

    fun setLocalPublicKey(pubKey: String) {
        this.pubKey = pubKey
    }

    fun importPublicKey() {
        if(isPublicKeyAddressValid(pubKey)) {
            _onConfirm.postValue(Unit)
        } else {
            _onInputRejected.postValue(Unit)
            invalidAddressErrors++

            if(invalidAddressErrors % errorThreshold == 0) {
                _onDisplayExplanation.postValue(getApplication<PlaidApp>().getString(R.string.import_public_key_error_invalid_address))
            }
        }
    }

    fun getEntropyStrength() = entropyStrength

    fun getLocalBipType() = bip

    fun entropyStrengthToString(context: Context) : String {
        when(getEntropyStrength()) {
            Mnemonic.EntropyStrength.Default -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_1)
            }
            Mnemonic.EntropyStrength.Low -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_2)
            }
            Mnemonic.EntropyStrength.Medium -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_3)
            }
            Mnemonic.EntropyStrength.High -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_4)
            }
            Mnemonic.EntropyStrength.VeryHigh -> {
                return context.getString(R.string.create_wallet_advanced_options_entropy_strength_5)
            }
            else -> {
                return "unknown"
            }
        }
    }

    fun setLocalSeedPhrase(p: List<String>) {
        seed = p.toMutableList()
    }

    fun showLocalSeedPhrase() {
        _seedPhraseGenerated.postValue(seed)
    }

    fun commitWalletToDisk(walletName: String) {
        if(pubKey.isNotEmpty()) {
            commitWalletToDisk(
                walletName = walletName,
                pubKey = pubKey
            )
        } else {
            commitWalletToDisk(
                walletName = walletName,
                seed = seed,
                bip = bip,
                testNetWallet = useTestNet
            )
        }
    }

    companion object {
        private const val errorThreshold = 3
        private const val maxWords = 24
    }
}