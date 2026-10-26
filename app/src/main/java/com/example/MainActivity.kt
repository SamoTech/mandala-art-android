package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.analytics.AnalyticsTracker
import com.example.data.MandalaDatabase
import com.example.data.MandalaRepository
import com.example.generator.MandalaLibrary
import com.example.model.MandalaCategory
import com.example.model.SavedArtwork
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.gallery.GalleryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.myart.MyArtScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MandalaTheme
import com.example.ui.theme.PinkAccent
import com.example.util.AppSettings
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data class Gallery(val category: MandalaCategory? = null) : Screen()
    data class Editor(val mandalaId: String) : Screen()
    data object MyArt : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MandalaDatabase.getInstance(applicationContext)
        val repository = MandalaRepository(database.mandalaDao())
        val appSettings = AppSettings(applicationContext)
        val adMobManager = AdMobManager.getInstance(applicationContext)

        // Initialize Google UMP Consent and AdMob SDK
        try {
            adMobManager.initializeConsentAndAds(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Exception during AdMob initialization: ${e.message}", e)
        }

        setContent {
            MandalaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MandalaApp(
                        activity = this,
                        repository = repository,
                        appSettings = appSettings,
                        adMobManager = adMobManager
                    )
                }
            }
        }
    }
}

@Composable
fun MandalaApp(
    activity: Activity,
    repository: MandalaRepository,
    appSettings: AppSettings,
    adMobManager: AdMobManager
) {
    val context = LocalContext.current
    val screenStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    val scope = rememberCoroutineScope()
    val allProgressList by repository.allProgress.collectAsState(initial = emptyList())
    val savedArtworksMap = remember(allProgressList) {
        allProgressList.associateBy { it.mandalaId }
    }
    val outlineColor by appSettings.outlineColor.collectAsState()
    val unlockedDesigns by appSettings.unlockedDesigns.collectAsState()

    var bonusToUnlock by remember { mutableStateOf<String?>(null) }
    var adUnavailableTarget by remember { mutableStateOf<String?>(null) }
    var isAdRequestInProgress by remember { mutableStateOf(false) }

    fun navigateTo(screen: Screen) {
        screenStack.add(screen)
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is Screen.Home -> {
                HomeScreen(
                    savedArtworks = savedArtworksMap,
                    outlineColor = outlineColor,
                    isDesignUnlocked = { id -> appSettings.isDesignUnlocked(id) },
                    onSelectMandala = { id -> navigateTo(Screen.Editor(id)) },
                    onRequestUnlockBonus = { id -> bonusToUnlock = id },
                    onNavigateToGallery = { cat -> navigateTo(Screen.Gallery(cat)) },
                    onNavigateToMyArt = { navigateTo(Screen.MyArt) },
                    onNavigateToSettings = { navigateTo(Screen.Settings) },
                    onToggleFavorite = { id, currentFav ->
                        scope.launch {
                            repository.toggleFavorite(id, currentFav)
                        }
                    }
                )
            }

            is Screen.Gallery -> {
                GalleryScreen(
                    initialCategory = screen.category,
                    savedArtworks = savedArtworksMap,
                    outlineColor = outlineColor,
                    isDesignUnlocked = { id -> appSettings.isDesignUnlocked(id) },
                    onSelectMandala = { id -> navigateTo(Screen.Editor(id)) },
                    onRequestUnlockBonus = { id -> bonusToUnlock = id },
                    onToggleFavorite = { id, currentFav ->
                        scope.launch {
                            repository.toggleFavorite(id, currentFav)
                        }
                    },
                    onNavigateBack = { navigateBack() }
                )
            }

            is Screen.Editor -> {
                val editorViewModel: EditorViewModel = viewModel(
                    key = screen.mandalaId,
                    factory = EditorViewModel.Factory(
                        mandalaId = screen.mandalaId,
                        repository = repository,
                        appSettings = appSettings
                    )
                )

                EditorScreen(
                    viewModel = editorViewModel,
                    appSettings = appSettings,
                    onNavigateBack = {
                        // Natural transition: show interstitial if available when leaving editor
                        adMobManager.showInterstitialIfAvailable(activity) {
                            navigateBack()
                        }
                    }
                )
            }

            is Screen.MyArt -> {
                MyArtScreen(
                    savedArtworks = savedArtworksMap,
                    outlineColor = outlineColor,
                    onSelectMandala = { id -> navigateTo(Screen.Editor(id)) },
                    onToggleFavorite = { id, currentFav ->
                        scope.launch {
                            repository.toggleFavorite(id, currentFav)
                        }
                    },
                    onDeleteArtwork = { id ->
                        scope.launch {
                            repository.deleteProgress(id)
                        }
                    },
                    onNavigateToGallery = { navigateTo(Screen.Gallery(null)) },
                    onNavigateBack = { navigateBack() }
                )
            }

            is Screen.Settings -> {
                SettingsScreen(
                    appSettings = appSettings,
                    onManageConsent = {
                        adMobManager.showPrivacyOptionsForm(activity)
                    },
                    onNavigateBack = { navigateBack() }
                )
            }
        }
    }

    // Rewarded Ad Voluntary Unlock Dialog
    bonusToUnlock?.let { targetId ->
        AnalyticsTracker.logEvent("rewarded_ad_offered", "content_type", "design")
        val targetDesign = MandalaLibrary.getDesignById(targetId)
        AlertDialog(
            onDismissRequest = {
                if (!isAdRequestInProgress) {
                    bonusToUnlock = null
                }
            },
            title = {
                Text(
                    text = "Unlock Bonus Artwork",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Would you like to watch a short sponsored video to unlock \"${targetDesign.title}\" permanently? You can also continue enjoying all 22 free designs anytime."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isAdRequestInProgress) return@Button
                        isAdRequestInProgress = true
                        AnalyticsTracker.logEvent("rewarded_ad_started", "content_type", "design")
                        val idToUnlock = targetId
                        bonusToUnlock = null

                        val designTitle = targetDesign.title
                        val rewardGranted = java.util.concurrent.atomic.AtomicBoolean(false)
                        val navigated = java.util.concurrent.atomic.AtomicBoolean(false)

                        adMobManager.showRewardedAd(
                            activity = activity,
                            onUserEarnedReward = { _ ->
                                if (rewardGranted.compareAndSet(false, true)) {
                                    AnalyticsTracker.logEvent("rewarded_ad_completed", "content_type", "design")
                                    appSettings.unlockDesign(idToUnlock)
                                    AnalyticsTracker.logEvent("premium_unlock_granted", "content_type", "design")
                                    Toast.makeText(context, "Unlocked \"$designTitle\"! Enjoy coloring.", Toast.LENGTH_SHORT).show()
                                    if (navigated.compareAndSet(false, true)) {
                                        navigateTo(Screen.Editor(idToUnlock))
                                    }
                                }
                            },
                            onAdClosed = { earned ->
                                isAdRequestInProgress = false
                                if (!earned && !appSettings.isDesignUnlocked(idToUnlock)) {
                                    // Ad was closed or dismissed without earning reward: NEVER unlock bonus design
                                    Toast.makeText(
                                        context,
                                        "Video was not completed. Watch the full video to unlock \"$designTitle\", or enjoy all 22 free designs anytime.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onAdUnavailable = {
                                isAdRequestInProgress = false
                                adUnavailableTarget = idToUnlock
                            },
                            onAdFailedToShow = { _ ->
                                isAdRequestInProgress = false
                                adUnavailableTarget = idToUnlock
                            }
                        )
                    },
                    enabled = !isAdRequestInProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_watch_rewarded_ad")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Watch Video", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isAdRequestInProgress) {
                            bonusToUnlock = null
                        }
                    },
                    modifier = Modifier.testTag("btn_dismiss_unlock_dialog")
                ) {
                    Text("Later")
                }
            }
        )
    }

    // Ad Unavailable / Retry Dialog
    adUnavailableTarget?.let { targetId ->
        val targetDesign = MandalaLibrary.getDesignById(targetId)
        AlertDialog(
            onDismissRequest = { adUnavailableTarget = null },
            title = {
                Text(
                    text = "Video Ad Unavailable",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "The sponsored video for \"${targetDesign.title}\" is not ready yet. Please check your internet connection and try again in a few moments.\n\nAll 22 standard mandala designs remain free and open to color without ads!"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val retryId = targetId
                        adUnavailableTarget = null
                        adMobManager.loadRewarded(activity)
                        if (adMobManager.isRewardedAdReady) {
                            bonusToUnlock = retryId
                        } else {
                            Toast.makeText(
                                context,
                                "Requesting video ad... Please try again in a moment.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_retry_ad")
                ) {
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { adUnavailableTarget = null },
                    modifier = Modifier.testTag("btn_dismiss_unavailable_dialog")
                ) {
                    Text("Explore Free Designs")
                }
            }
        )
    }
}
