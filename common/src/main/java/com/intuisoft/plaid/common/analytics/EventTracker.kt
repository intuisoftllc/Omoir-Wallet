package com.intuisoft.plaid.common.analytics

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

class EventTracker(
    private val application: Application
) {

    private var firebaseAnalytics: FirebaseAnalytics? = null
    get() {
        if(field == null) {
            field = FirebaseAnalytics.getInstance(application)
        }

        return field
    }

    fun log(event: Event) {
        firebaseAnalytics?.logEvent(event.event_name) {
            event.event_params.forEach {
                this.param(it.first, it.second)
            }
        }
    }

    inline fun logIf(event: Event, predicate: () -> Boolean) {
        if(predicate()) {
            log(event)
        }
    }
}