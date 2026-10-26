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
        val targetDesign = MandalaLibrary.getDesignById(targetId)
        AlertDialog(
            onDismissRequest = { bonusToUnlock = null },
            title = {
                Text(
                    text = "Unlock Bonus Artwork",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Would you like to watch a short sponsored video to unlock \"${targetDesign.title}\" permanently? You can also continue enjoying the 22 free designs anytime."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        bonusToUnlock = null
                        adMobManager.showRewardedAd(
                            activity = activity,
                            onUserEarnedReward = {
                                appSettings.unlockDesign(targetId)
                                Toast.makeText(context, "Unlocked \"${targetDesign.title}\"!", Toast.LENGTH_SHORT).show()
                                navigateTo(Screen.Editor(targetId))
                            },
                            onAdClosed = {
                                // If offline or ad failed to load, gracefully unlock as a gift so user is never blocked
                                if (!appSettings.isDesignUnlocked(targetId)) {
                                    appSettings.unlockDesign(targetId)
                                    Toast.makeText(context, "Bonus design unlocked! Enjoy.", Toast.LENGTH_SHORT).show()
                                    navigateTo(Screen.Editor(targetId))
                                }
                            }
                        )
                    },
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
                    onClick = { bonusToUnlock = null },
                    modifier = Modifier.testTag("btn_dismiss_unlock_dialog")
                ) {
                    Text("Later")
                }
            }
        )
    }
}
