package com.example.ads

import com.example.BuildConfig
import com.google.android.gms.ads.RequestConfiguration

/**
 * AdMob configuration and ad unit IDs.
 *
 * NOTE: Separated cleanly between Debug and Release variants via Gradle BuildConfig:
 * - Debug: Uses Google's official test App ID and test Rewarded ad unit ID.
 * - Release: Uses verified production AdMob App ID and Rewarded ad unit ID.
 * - In this phase, only the Rewarded format (Mandala_Rewarded_Unlock) is active.
 */
object AdMobConstants {

    /**
     * Official AdMob Publisher ID: pub-3703596518277340
     * Recorded in app-ads.txt for publisher verification.
     */
    const val PUBLISHER_ID = "pub-3703596518277340"

    /**
     * Official Rewarded Ad Unit Name and reward metadata configured in Google AdMob:
     * Unit Name: Mandala_Rewarded_Unlock
     * Reward Item: Design Unlock
     * Reward Amount: 1
     */
    const val REWARDED_UNIT_NAME = "Mandala_Rewarded_Unlock"
    const val REWARD_ITEM_TYPE = "Design Unlock"
    const val REWARD_AMOUNT = 1

    /**
     * Official production identifiers (used for release builds & verification)
     */
    const val PROD_ADMOB_APP_ID = "ca-app-pub-3703596518277340~5699100454"
    const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-3703596518277340/5892552476"

    /**
     * Official Google test identifiers (used for debug builds & testing)
     */
    const val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    /**
     * Whether the application is running in test ad mode.
     * Evaluated from BuildConfig to guarantee test IDs in debug and real IDs in release.
     */
    val USE_TEST_ADS: Boolean
        get() = BuildConfig.USE_TEST_ADS

    /**
     * AdMob App ID configured for the current build variant.
     */
    val adMobAppId: String
        get() = BuildConfig.ADMOB_APP_ID

    /**
     * Rewarded ad unit ID configured for the current build variant.
     */
    val rewardedAdUnitId: String
        get() = BuildConfig.REWARDED_AD_UNIT_ID

    // ==========================================
    // BANNER & INTERSTITIAL (DISABLED IN RELEASE IN THIS PHASE)
    // ==========================================
    // In this phase, only the Rewarded format is active in production.
    // In debug mode, test IDs are supplied for local layout inspection; in release they are empty.
    val bannerAdUnitId: String
        get() = if (USE_TEST_ADS) "ca-app-pub-3940256099942544/9214589741" else ""

    val interstitialAdUnitId: String
        get() = if (USE_TEST_ADS) "ca-app-pub-3940256099942544/1033173712" else ""

    /**
     * Content rating suitable for general audiences (G rating for calming art app).
     */
    const val MAX_AD_CONTENT_RATING = RequestConfiguration.MAX_AD_CONTENT_RATING_G
    const val TAG_FOR_CHILD_DIRECTED_TREATMENT = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
}
