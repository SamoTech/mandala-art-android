package com.example.ads

import com.google.android.gms.ads.RequestConfiguration

/**
 * AdMob configuration and ad unit IDs.
 *
 * NOTE: As required by Google AdMob guidelines:
 * - Test ad unit IDs are used during development and testing to prevent policy violations.
 * - Production AdMob App ID and Ad Unit IDs must be configured here when publishing to Google Play.
 * - The Publisher ID (pub-3703596518277340) is kept distinct from Ad Unit IDs and Application IDs.
 */
object AdMobConstants {

    /**
     * Official AdMob Publisher ID: pub-3703596518277340
     * Recorded in app-ads.txt for publisher verification.
     */
    const val PUBLISHER_ID = "pub-3703596518277340"

    /**
     * Whether the application is running in test ad mode.
     * Always set to true during development to avoid policy strikes.
     */
    const val USE_TEST_ADS = true

    // ==========================================
    // OFFICIAL GOOGLE TEST AD UNIT IDS (Android)
    // ==========================================
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741" // Adaptive banner test ID
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Interstitial test ID
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Rewarded test ID

    // ==========================================
    // PRODUCTION AD UNIT PLACEHOLDERS
    // Replace with your real AdMob unit IDs when deploying to production
    // ==========================================
    private const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-3703596518277340/YOUR_BANNER_ID"
    private const val PROD_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3703596518277340/YOUR_INTERSTITIAL_ID"
    private const val PROD_REWARDED_AD_UNIT_ID = "ca-app-pub-3703596518277340/YOUR_REWARDED_ID"

    val bannerAdUnitId: String
        get() = if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID else PROD_BANNER_AD_UNIT_ID

    val interstitialAdUnitId: String
        get() = if (USE_TEST_ADS) TEST_INTERSTITIAL_AD_UNIT_ID else PROD_INTERSTITIAL_AD_UNIT_ID

    val rewardedAdUnitId: String
        get() = if (USE_TEST_ADS) TEST_REWARDED_AD_UNIT_ID else PROD_REWARDED_AD_UNIT_ID

    /**
     * Content rating suitable for general audiences (G rating for calming art app).
     */
    const val MAX_AD_CONTENT_RATING = RequestConfiguration.MAX_AD_CONTENT_RATING_G
    const val TAG_FOR_CHILD_DIRECTED_TREATMENT = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
}
