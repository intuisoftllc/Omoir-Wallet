package io.horizontalsystems.bitcoincore.transactions

import com.intuisoft.plaid.common.coroutines.OmoirScope
import com.intuisoft.plaid.common.util.Constants
import kotlinx.coroutines.*

class TransactionSendTimer(private val period: Long) {

    interface Listener {
        fun onTimePassed()
    }

    var listener: Listener? = null
    private var task: Job? = null

    @Synchronized
    fun startIfNotRunning() {
        if (task == null) {
            task = OmoirScope.applicationScope.launch(Dispatchers.IO) {
                while(true) {
                    this.ensureActive()
                    listener?.onTimePassed()
                    delay(period * Constants.Time.MILLS_PER_SEC)
                }
            }
        }
    }

    @Synchronized
    fun stop() {
        task?.let {
            it.cancel()
            task = null
        }
    }

}
