package com.intuisoft.plaid.walletmanager.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.model.AssetTransferStatus
import com.intuisoft.plaid.common.model.BatchDataModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.koin.java.KoinJavaComponent.inject
import java.security.SecureRandom

class AtpStatusWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val channel = "atp_channel"
    private var notificationId: Int = SecureRandom().nextInt()
    private var lastBatch: BatchDataModel? = null
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    private val localStoreRepository: LocalStoreRepository by inject(LocalStoreRepository::class.java)
    private val walletManager: AbstractWalletManager by inject(AbstractWalletManager::class.java)

    @NonNull
    override suspend fun doWork(): Result {
        val walletId = workerParams.inputData.getString(WALLET_KEY) ?: return Result.failure()
        val transferId = workerParams.inputData.getString(ATP_KEY) ?: return Result.failure()
        var transfer = getTransfer(walletId, transferId)

        if(transfer != null) {
            try {
                val converter = RateConverter(0.0).setLocalRate(
                    RateConverter.RateType.SATOSHI_RATE,
                    transfer.expectedAmount.toDouble()
                )

                val sendWallet = walletManager.findLocalWallet(transfer.recipientWallet)?.name
                createNotification(context.getString(R.string.atp_notification_subtitle_1))

                while (transfer != null && transfer.status <= AssetTransferStatus.IN_PROGRESS && walletManager.isRunning()) {
                    val batches = localStoreRepository.getBatchDataForTransfer(transferId)
                    val finishedBatches =
                        batches.count { it.status >= AssetTransferStatus.PARTIALLY_COMPLETED }
                    val currentBatch =
                        batches.findLast { it.status == AssetTransferStatus.WAITING || it.status == AssetTransferStatus.IN_PROGRESS }

                    if (CommonService.getUserData() != null && sendWallet == null) {
                        break
                    }

                    if (currentBatch != null && currentBatch != lastBatch && sendWallet != null) {
                        lastBatch = currentBatch

                        if (currentBatch.status == AssetTransferStatus.WAITING) {
                            createProgressNotification(
                                context.getString(
                                    R.string.atp_notification_subtitle_3,
                                    currentBatch.blocksRemaining.toString(),
                                    (currentBatch.blocksRemaining * 10).toString()
                                ),
                                batches.size, finishedBatches, true
                            )
                        } else {
                            createProgressNotification(
                                context.getString(
                                    R.string.atp_notification_subtitle_2,
                                    converter.from(RateConverter.RateType.BTC_RATE, "").second,
                                    sendWallet,
                                    finishedBatches,
                                    batches.size
                                ),
                                batches.size, finishedBatches, false
                            )
                        }
                    }

                    transfer = getTransfer(walletId, transferId)
                    delay(5L * Constants.Time.MILLS_PER_SEC)
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                } else throw e
            } finally {
                notificationManager.cancel(notificationId)
            }
        }

        return Result.success()
    }

    private fun getTransfer(walletId: String, id: String) =
        localStoreRepository.getAllAssetTransfers(walletId)
            .find { it.id == id }
    private fun createNotification(message: String) {
        val title = context.getString(R.string.atp_notification_title)
        createChannel()
        val mBuilder = NotificationCompat.Builder(context,  channel)

        mBuilder
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(message)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)

        setForegroundAsync(ForegroundInfo(notificationId, mBuilder.build()))
    }
    private fun createProgressNotification(message: String, max: Int, progress: Int, indeterminate: Boolean) {
        val title = context.getString(R.string.atp_notification_title)
        createChannel()
        val mBuilder = NotificationCompat.Builder(context,  channel)

        mBuilder
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(message)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setProgress(max, progress, indeterminate)


        setForegroundAsync(ForegroundInfo(notificationId, mBuilder.build()))
    }

    private fun createChannel() { // Create the NotificationChannel
        val name = context.getString(R.string.atp_channel_name)
        val descriptionText = context.getString(R.string.atp_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(channel, name, importance)
        mChannel.description = descriptionText
        notificationManager.createNotificationChannel(mChannel)
    }

    companion object {
        val ATP_KEY = "TRANSFER_ID"
        val WALLET_KEY = "WALLET_ID"
    }
}