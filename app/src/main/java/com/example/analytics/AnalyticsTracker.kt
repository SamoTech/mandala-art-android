package com.example.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Minimal, privacy-conscious analytics facade.
 *
 * Analytics remains a no-op until a Firebase app is configured for this application.
 * Event parameters are deliberately limited to non-identifying content categories.
 */
object AnalyticsTracker {
    private const val TAG = "AnalyticsTracker"

    @Volatile
    private var analytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.i(TAG, "Firebase is not configured; analytics disabled.")
                return
            }
            analytics = FirebaseAnalytics.getInstance(context.applicationContext)
        } catch (error: IllegalStateException) {
            Log.w(TAG, "Firebase Analytics unavailable; continuing without analytics.", error)
        } catch (error: RuntimeException) {
            Log.w(TAG, "Unable to initialize Firebase Analytics; continuing without analytics.", error)
        }
    }

    fun logEvent(
        eventName: String,
        parameterName: String? = null,
        parameterValue: String? = null
    ) {
        val instance = analytics ?: return
        val parameters = Bundle()
        if (parameterName != null && parameterValue != null) {
            parameters.putString(parameterName, parameterValue)
        }
        try {
            instance.logEvent(eventName, parameters)
        } catch (error: RuntimeException) {
            Log.w(TAG, "Analytics event could not be logged: $eventName", error)
        }
    }
}
