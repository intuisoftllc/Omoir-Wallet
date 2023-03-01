package com.intuisoft.plaid.common.analytics

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.analytics.ktx.setConsent
import com.google.firebase.ktx.Firebase
import com.intuisoft.plaid.common.local.AppPrefs
import com.intuisoft.plaid.common.repositories.LocalStoreRepository

class EventTracker(
    private val application: Application,
    private val localStoreRepository: LocalStoreRepository
) {

    private var firebaseAnalytics: FirebaseAnalytics? = null
    get() {
        if(field == null) {
            field = FirebaseAnalytics.getInstance(application)
        }

        return field
    }

    fun applyDataTrackingConsent() {
        val consent  = localStoreRepository.isTrackingUsageData()
        firebaseAnalytics?.setConsent {
            if(consent) {
                this.adStorage = FirebaseAnalytics.ConsentStatus.GRANTED
                this.analyticsStorage = FirebaseAnalytics.ConsentStatus.GRANTED
            } else {
                this.adStorage = FirebaseAnalytics.ConsentStatus.DENIED
                this.analyticsStorage = FirebaseAnalytics.ConsentStatus.DENIED
            }
        }
    }

    fun log(event: Event) {
        if(localStoreRepository.isTrackingUsageData()) {
            firebaseAnalytics?.logEvent(event.event_name) {
                event.event_params.forEach {
                    this.param(it.first, it.second)
                }
            }
        }
    }

    inline fun logIf(event: Event, predicate: () -> Boolean) {
        if(predicate()) {
            log(event)
        }
    }
}