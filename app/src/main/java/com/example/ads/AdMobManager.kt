package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
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
import com.google.android.ump.ConsentDebugSettings
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
            val paramsBuilder = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)

            // Safe consent-flow testing configuration: only enabled in debug builds, never in production
            if (BuildConfig.DEBUG) {
                val debugSettings = ConsentDebugSettings.Builder(activity).build()
                paramsBuilder.setConsentDebugSettings(debugSettings)
            }

            val params = paramsBuilder.build()
            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

            Log.d(TAG, "Requesting consent info update [isDebug=${BuildConfig.DEBUG}]")

            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    Log.d(
                        TAG,
                        "Consent info updated. Status: ${consentInformation.consentStatus}, canRequestAds: ${consentInformation.canRequestAds()}"
                    )
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                            if (formError != null) {
                                Log.w(TAG, "Consent form error [${formError.errorCode}]: ${formError.message}")
                            } else {
                                Log.d(TAG, "Consent form completed or not required")
                            }
                            if (consentInformation.canRequestAds()) {
                                initializeMobileAds(activity)
                            } else {
                                Log.w(TAG, "Ads cannot be requested under current consent status")
                            }
                        }
                    } else if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity)
                    }
                },
                { requestConsentError ->
                    Log.w(
                        TAG,
                        "Consent request error [${requestConsentError.errorCode}]: ${requestConsentError.message}"
                    )
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity)
                    }
                }
            )

            // If consent was previously granted, initialize immediately without waiting for network update
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

            Log.d(
                TAG,
                "Initializing MobileAds SDK [App ID: ${AdMobConstants.adMobAppId}, TestMode: ${AdMobConstants.USE_TEST_ADS}]"
            )

            // Initialize AdMob
            MobileAds.initialize(appContext) { status ->
                Log.d(TAG, "AdMob SDK Initialized. Adapter status: ${status.adapterStatusMap}")
                if (AdMobConstants.interstitialAdUnitId.isNotEmpty()) {
                    loadInterstitial(appContext)
                }
                loadRewarded(appContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during MobileAds.initialize: ${e.message}", e)
        }
    }

    // ==========================================
    // INTERSTITIAL ADS (DISABLED IN RELEASE IN THIS PHASE)
    // ==========================================

    fun loadInterstitial(context: Context) {
        val unitId = AdMobConstants.interstitialAdUnitId
        if (unitId.isEmpty()) {
            return // Not active in this phase
        }
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                unitId,
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
        if (AdMobConstants.interstitialAdUnitId.isEmpty() || activity.isFinishing || activity.isDestroyed) {
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
        val unitId = AdMobConstants.rewardedAdUnitId
        if (unitId.isBlank()) {
            Log.w(TAG, "Rewarded ad unit ID is blank, skipping load")
            return
        }
        if (rewardedAd != null || isRewardedLoading) {
            Log.d(
                TAG,
                "Rewarded ad request skipped (already loaded: ${rewardedAd != null}, loading: $isRewardedLoading)"
            )
            return
        }
        isRewardedLoading = true
        Log.d(
            TAG,
            "Requesting rewarded ad [unit: $unitId, unitName: ${AdMobConstants.REWARDED_UNIT_NAME}, isTestMode: ${AdMobConstants.USE_TEST_ADS}]"
        )

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context.applicationContext,
                unitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "Rewarded ad loaded successfully [unit: $unitId]")
                        rewardedAd = ad
                        isRewardedLoading = false
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(
                            TAG,
                            "Rewarded ad failed to load [code: ${loadAdError.code}, domain: ${loadAdError.domain}]: ${loadAdError.message}"
                        )
                        rewardedAd = null
                        isRewardedLoading = false
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception while initiating rewarded ad load: ${e.message}", e)
            rewardedAd = null
            isRewardedLoading = false
        }
    }

    val isRewardedAdReady: Boolean
        get() = rewardedAd != null

    /**
     * Shows a rewarded ad voluntarily chosen by the user.
     *
     * @param activity The host Activity
     * @param onUserEarnedReward Callback invoked ONLY when the user legitimately earns the reward
     * @param onAdClosed Callback invoked when the ad is closed or fails to show. Parameter [rewardEarned] indicates whether reward was granted.
     * @param onAdUnavailable Callback invoked if the ad is not ready / not loaded or offline
     * @param onAdFailedToShow Callback invoked if the ad fails during the show call
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdClosed: (rewardEarned: Boolean) -> Unit = {},
        onAdUnavailable: () -> Unit = {},
        onAdFailedToShow: (AdError) -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed; skipping rewarded ad")
            onAdClosed(false)
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            Log.d(TAG, "Rewarded ad not ready for display (isLoading: $isRewardedLoading). Reloading.")
            loadRewarded(activity)
            onAdUnavailable()
            onAdClosed(false)
            return
        }

        val rewardEarned = AtomicBoolean(false)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                val earned = rewardEarned.get()
                Log.d(TAG, "Rewarded ad dismissed full screen. Reward earned: $earned")
                rewardedAd = null
                loadRewarded(activity)
                onAdClosed(earned)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(
                    TAG,
                    "Rewarded ad failed to show [code: ${adError.code}, domain: ${adError.domain}]: ${adError.message}"
                )
                rewardedAd = null
                loadRewarded(activity)
                onAdFailedToShow(adError)
                onAdClosed(rewardEarned.get())
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad displayed full screen")
                rewardedAd = null
            }
        }

        try {
            Log.d(TAG, "Presenting rewarded ad to user for voluntary unlock")
            ad.show(activity) { rewardItem ->
                // Ensure reward is only granted once per ad impression
                if (rewardEarned.compareAndSet(false, true)) {
                    Log.d(
                        TAG,
                        "Confirmed user earned reward: ${rewardItem.type} (${rewardItem.amount}). Granting unlock."
                    )
                    onUserEarnedReward(rewardItem)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during rewarded ad.show: ${e.message}", e)
            rewardedAd = null
            loadRewarded(activity)
            onAdFailedToShow(AdError(0, e.message ?: "Failed to display ad", "Internal"))
            onAdClosed(rewardEarned.get())
        }
    }
}
