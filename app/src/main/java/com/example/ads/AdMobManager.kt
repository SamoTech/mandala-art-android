package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class AdMobManager(private val context: Context) {

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTimestamp = 0L

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    companion object {
        private const val TAG = "AdMobManager"
        // Cooldown between interstitial ads (e.g., 60 seconds minimum)
        private const val INTERSTITIAL_COOLDOWN_MS = 60_000L

        @Volatile
        private var INSTANCE: AdMobManager? = null

        fun getInstance(context: Context): AdMobManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AdMobManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Initializes Google User Messaging Platform (UMP) consent flow and AdMob SDK.
     */
    fun initializeConsentAndAds(activity: Activity) {
        try {
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                            if (formError != null) {
                                Log.w(TAG, "Consent form error [${formError.errorCode}]: ${formError.message}")
                            }
                            if (consentInformation.canRequestAds()) {
                                initializeMobileAds(activity)
                            }
                        }
                    } else if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity)
                    }
                },
                { requestConsentError ->
                    Log.w(TAG, "Consent request error [${requestConsentError.errorCode}]: ${requestConsentError.message}")
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity)
                    }
                }
            )

            // If consent was previously granted, initialize immediately
            if (consentInformation.canRequestAds()) {
                initializeMobileAds(activity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during initializeConsentAndAds: ${e.message}", e)
            try {
                initializeMobileAds(activity)
            } catch (inner: Exception) {
                Log.e(TAG, "Exception during fallback MobileAds initialization: ${inner.message}", inner)
            }
        }
    }

    /**
     * Shows Google UMP Privacy Options Form to allow users in the EEA/UK/CH to modify or revoke consent choices.
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismiss: () -> Unit = {}) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed; skipping privacy options form")
            onDismiss()
            return
        }
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                if (formError != null) {
                    Log.w(TAG, "Privacy options form error [${formError.errorCode}]: ${formError.message}")
                }
                onDismiss()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during showPrivacyOptionsForm: ${e.message}", e)
            onDismiss()
        }
    }

    fun isPrivacyOptionsRequired(): Boolean {
        return try {
            UserMessagingPlatform.getConsentInformation(context).privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } catch (e: Exception) {
            Log.w(TAG, "Exception checking isPrivacyOptionsRequired: ${e.message}")
            false
        }
    }

    private fun initializeMobileAds(context: Context) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        val appContext = context.applicationContext

        try {
            // Configure content rating and child-directed settings
            val requestConfig = RequestConfiguration.Builder()
                .setMaxAdContentRating(AdMobConstants.MAX_AD_CONTENT_RATING)
                .setTagForChildDirectedTreatment(AdMobConstants.TAG_FOR_CHILD_DIRECTED_TREATMENT)
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            // Initialize AdMob
            MobileAds.initialize(appContext) { status ->
                Log.d(TAG, "AdMob SDK Initialized: ${status.adapterStatusMap}")
                // Preload interstitial and rewarded ads in background
                loadInterstitial(appContext)
                loadRewarded(appContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during MobileAds.initialize: ${e.message}", e)
        }
    }

    // ==========================================
    // INTERSTITIAL ADS
    // ==========================================

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                AdMobConstants.interstitialAdUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Interstitial ad loaded successfully")
                        interstitialAd = ad
                        isInterstitialLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "Interstitial ad failed to load [${loadAdError.code}]: ${loadAdError.message}")
                        interstitialAd = null
                        isInterstitialLoading = false
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception while initiating interstitial ad load", e)
            interstitialAd = null
            isInterstitialLoading = false
        }
    }

    /**
     * Shows an interstitial ad if available and cooldown has elapsed.
     * Always calls [onDismiss] whether the ad was shown, failed, or was not ready,
     * ensuring that gameplay and navigation are NEVER blocked.
     */
    fun showInterstitialIfAvailable(activity: Activity, onDismiss: () -> Unit) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed; skipping interstitial ad")
            onDismiss()
            return
        }

        val now = System.currentTimeMillis()
        val ad = interstitialAd

        if (ad != null && (now - lastInterstitialShownTimestamp >= INTERSTITIAL_COOLDOWN_MS)) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed")
                    interstitialAd = null
                    lastInterstitialShownTimestamp = System.currentTimeMillis()
                    loadInterstitial(activity)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial failed to show [${adError.code}]: ${adError.message}")
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismiss()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed full screen")
                    interstitialAd = null
                }
            }
            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Exception during interstitial ad.show", e)
                interstitialAd = null
                onDismiss()
            }
        } else {
            // Not ready or cooldown active; proceed with navigation immediately
            onDismiss()
            if (interstitialAd == null) {
                loadInterstitial(activity)
            }
        }
    }

    // ==========================================
    // REWARDED ADS
    // ==========================================

    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context.applicationContext,
                AdMobConstants.rewardedAdUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "Rewarded ad loaded successfully")
                        rewardedAd = ad
                        isRewardedLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "Rewarded ad failed to load [${loadAdError.code}]: ${loadAdError.message}")
                        rewardedAd = null
                        isRewardedLoading = false
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception while initiating rewarded ad load", e)
            rewardedAd = null
            isRewardedLoading = false
        }
    }

    val isRewardedAdReady: Boolean
        get() = rewardedAd != null

    /**
     * Shows a rewarded ad voluntarily chosen by the user.
     * @param onUserEarnedReward callback invoked when user earns reward.
     * @param onAdClosed callback invoked when ad finishes or fails to show.
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdClosed: () -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed; skipping rewarded ad")
            onAdClosed()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    rewardedAd = null
                    loadRewarded(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded ad failed to show [${adError.code}]: ${adError.message}")
                    rewardedAd = null
                    loadRewarded(activity)
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed")
                    rewardedAd = null
                }
            }
            try {
                ad.show(activity) { rewardItem ->
                    onUserEarnedReward(rewardItem)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during rewarded ad.show", e)
                rewardedAd = null
                onAdClosed()
            }
        } else {
            // If offline or ad wasn't available, notify completion
            Log.d(TAG, "Rewarded ad not ready")
            loadRewarded(activity)
            onAdClosed()
        }
    }
}
