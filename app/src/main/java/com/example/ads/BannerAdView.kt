package com.example.ads

import android.util.DisplayMetrics
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val configuredBannerId = AdMobConstants.bannerAdUnitId
    if (configuredBannerId.isEmpty()) {
        // Banner format is disabled in this phase (no production banner unit configured)
        return
    }

    val context = LocalContext.current
    var adViewInstance by remember { mutableStateOf<AdView?>(null) }
    var isAdLoaded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                adViewInstance?.destroy()
            } catch (e: Exception) {
                android.util.Log.w("BannerAdView", "Error destroying AdView: ${e.message}")
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (isAdLoaded) 50.dp else 0.dp)
            .testTag("banner_ad_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                try {
                    AdView(ctx).apply {
                        adViewInstance = this
                        setAdUnitId(configuredBannerId)

                        // Calculate adaptive banner width based on screen width
                        val displayMetrics: DisplayMetrics = ctx.resources.displayMetrics
                        val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt().coerceAtLeast(320)
                        try {
                            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                        } catch (e: Exception) {
                            android.util.Log.w("BannerAdView", "Failed to compute anchored adaptive banner size, falling back to standard BANNER: ${e.message}")
                            setAdSize(AdSize.BANNER)
                        }

                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                android.util.Log.w("BannerAdView", "Banner ad failed to load [${loadAdError.code}]: ${loadAdError.message}")
                                isAdLoaded = false
                            }
                        }

                        try {
                            loadAd(AdRequest.Builder().build())
                        } catch (e: Exception) {
                            android.util.Log.w("BannerAdView", "Exception during banner loadAd: ${e.message}")
                            isAdLoaded = false
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BannerAdView", "Failed to instantiate AdView (e.g. WebView unavailable): ${e.message}", e)
                    android.view.View(ctx)
                }
            }
        )
    }
}
