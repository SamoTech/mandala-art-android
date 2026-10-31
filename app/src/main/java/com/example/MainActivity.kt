package com.example

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.analytics.AnalyticsTracker
import com.example.data.CloudSaveManager
import com.example.data.DeviceIdentity
import com.example.data.MandalaDatabase
import com.example.data.MandalaRepository
import com.example.generator.MandalaLibrary
import com.example.model.LevelSystem
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
import com.example.ui.theme.mandalaGlassGradient
import com.example.util.AppSettings
import com.example.util.StartupRestoreGate
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data class Gallery(val category: MandalaCategory? = null) : Screen()
    data class Editor(val mandalaId: String) : Screen()
    data object MyArt : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val languageTag = newBase.getSharedPreferences("mandala_app_prefs", Context.MODE_PRIVATE)
            .getString(AppSettings.KEY_LANGUAGE, "").orEmpty()
        if (languageTag.isBlank()) {
            super.attachBaseContext(newBase)
        } else {
            val configuration = Configuration(newBase.resources.configuration)
            configuration.setLocales(LocaleList.forLanguageTags(languageTag))
            super.attachBaseContext(newBase.createConfigurationContext(configuration))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = MandalaDatabase.getInstance(applicationContext)
        val deviceActorId = DeviceIdentity.getOrCreate(applicationContext)
        val repository = MandalaRepository(database.mandalaDao(), deviceActorId)
        val cloudSaveManager = CloudSaveManager(database.mandalaDao())
        val appSettings = AppSettings(applicationContext)
        AnalyticsTracker.initialize(applicationContext)
        val adMobManager = AdMobManager.getInstance(applicationContext)

        // Initialize Google UMP Consent and AdMob SDK
        try {
            adMobManager.initializeConsentAndAds(this)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Exception during AdMob initialization: ${e.message}", e)
        }

        setContent {
            MandalaTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = mandalaGlassGradient())
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        MandalaApp(
                            activity = this@MainActivity,
                            repository = repository,
                            appSettings = appSettings,
                            adMobManager = adMobManager,
                            cloudSaveManager = cloudSaveManager
                        )
                    }
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
    adMobManager: AdMobManager,
    cloudSaveManager: CloudSaveManager
) {
    val context = LocalContext.current

    val screenStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    var observedOwnerUid by remember { mutableStateOf(repository.currentOwnerUid) }
    val startupRestoreGate = remember { StartupRestoreGate() }
    var startupRestoreRequested by remember { mutableStateOf(false) }
    var cloudRestoreLoading by remember { mutableStateOf(false) }
    var cloudRestoreFailed by remember { mutableStateOf(false) }
    var cloudRestoreRetryCount by remember { mutableStateOf(0) }

    DisposableEffect(repository, cloudSaveManager) {
        val stopObserving = cloudSaveManager.observeAuthState { uid ->
            // Firebase may report null before persisted auth finishes restoring.
            // Trigger restore on the first non-empty UID, not on the first callback.
            if (startupRestoreGate.onAuthStateChanged(uid) != null) {
                startupRestoreRequested = true
            }
            repository.setOwnerUid(uid)
            if (uid != observedOwnerUid) {
                observedOwnerUid = uid
                screenStack.clear()
                screenStack.add(Screen.Home)
            }
        }
        onDispose { stopObserving() }
    }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    val scope = rememberCoroutineScope()

    // Restore only the already-authenticated account on cold start. Sign-in and
    // account creation retain their existing explicit sync path; anonymous and
    // other-account partitions are never imported here.
    LaunchedEffect(startupRestoreRequested, cloudRestoreRetryCount) {
        if (startupRestoreRequested) {
            val startupUid = observedOwnerUid
            if (startupUid.isNotBlank() && cloudSaveManager.currentUser?.uid == startupUid) {
                cloudRestoreLoading = true
                cloudRestoreFailed = false
                try {
                    cloudSaveManager.syncProgress()
                } catch (error: Exception) {
                    android.util.Log.w("MainActivity", "Startup cloud restore failed; local progress remains available.", error)
                    cloudRestoreFailed = true
                } finally {
                    cloudRestoreLoading = false
                    startupRestoreRequested = false
                }
            } else {
                startupRestoreRequested = false
            }
        }
    }

    val allProgressList by repository.allProgress.collectAsState(initial = emptyList())
    val savedArtworksMap = remember(allProgressList) {
        allProgressList.associateBy { it.mandalaId }
    }
    val outlineColor by appSettings.outlineColor.collectAsState()
    val unlockedDesigns by appSettings.unlockedDesigns.collectAsState()

    var bonusToUnlock by remember { mutableStateOf<String?>(null) }
    var adUnavailableTarget by remember { mutableStateOf<String?>(null) }
    var isAdRequestInProgress by remember { mutableStateOf(false) }
    var lockedLevelTarget by remember { mutableStateOf<String?>(null) }

    fun navigateTo(screen: Screen) {
        screenStack.add(screen)
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
        }
    }

    // Combined unlock gate: premium (ad-unlock) designs plus free level progression.
    val isDesignOpen: (String) -> Boolean = { id ->
        appSettings.isDesignUnlocked(id) &&
            LevelSystem.isDesignUnlockedByProgress(id, savedArtworksMap)
    }

    val requestUnlock: (String) -> Unit = { id ->
        if (id in LevelSystem.ALL_LEVEL_DESIGN_IDS && !isDesignOpen(id)) {
            lockedLevelTarget = id
        } else {
            bonusToUnlock = id
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
                    isDesignUnlocked = { id -> isDesignOpen(id) },
                    onSelectMandala = { id -> navigateTo(Screen.Editor(id)) },
                    onRequestUnlockBonus = { id -> requestUnlock(id) },
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
                    isDesignUnlocked = { id -> isDesignOpen(id) },
                    onSelectMandala = { id -> navigateTo(Screen.Editor(id)) },
                    onRequestUnlockBonus = { id -> requestUnlock(id) },
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
                    key = "${repository.currentOwnerUid}:${screen.mandalaId}",
                    factory = EditorViewModel.Factory(
                        mandalaId = screen.mandalaId,
                        repository = repository,
                        appSettings = appSettings,
                        ownerUid = repository.currentOwnerUid
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
                    isDesignUnlocked = { id -> isDesignOpen(id) },
                    onRequestUnlock = { id -> requestUnlock(id) },
                    onNavigateToGallery = { navigateTo(Screen.Gallery(null)) },
                    onNavigateBack = { navigateBack() }
                )
            }

            is Screen.Settings -> {
                SettingsScreen(
                    appSettings = appSettings,
                    cloudSaveManager = cloudSaveManager,
                    onLanguageChange = { languageTag ->
                        appSettings.setLanguage(languageTag)
                        activity.recreate()
                    },
                    onManageConsent = {
                        adMobManager.showPrivacyOptionsForm(activity)
                    },
                    onNavigateBack = { navigateBack() }
                )
            }
        }
    }

    if (cloudRestoreLoading) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.settings_cloud_restore_title)) },
            text = { Text(stringResource(R.string.settings_cloud_restore_message)) },
            confirmButton = {}
        )
    }

    if (cloudRestoreFailed) {
        AlertDialog(
            onDismissRequest = { cloudRestoreFailed = false },
            title = { Text(stringResource(R.string.settings_cloud_restore_failed_title)) },
            text = { Text(stringResource(R.string.settings_cloud_restore_failed_message)) },
            confirmButton = {
                TextButton(onClick = {
                    cloudRestoreFailed = false
                    startupRestoreRequested = true
                    cloudRestoreRetryCount += 1
                }) { Text(stringResource(R.string.dialog_retry)) }
            },
            dismissButton = {
                TextButton(onClick = { cloudRestoreFailed = false }) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }

    // Rewarded Ad Voluntary Unlock Dialog
    bonusToUnlock?.let { targetId ->
        LaunchedEffect(targetId) {
            AnalyticsTracker.logEvent("rewarded_ad_offered", "content_type", "design")
        }
        val targetDesign = MandalaLibrary.getDesignById(targetId)
        AlertDialog(
            onDismissRequest = {
                if (!isAdRequestInProgress) {
                    bonusToUnlock = null
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.reward_unlock_bonus_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.reward_unlock_bonus_message, targetDesign.title, 52)
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
                                    Toast.makeText(context, context.getString(R.string.reward_design_unlocked_toast, designTitle), Toast.LENGTH_SHORT).show()
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
                                        context.getString(R.string.reward_video_not_completed, designTitle, 52),
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
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.testTag("btn_watch_rewarded_ad")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.reward_watch_video), fontWeight = FontWeight.Bold)
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
                    Text(stringResource(R.string.dialog_later))
                }
            }
        )
    }

    // Level Progression Locked Dialog
    lockedLevelTarget?.let { targetId ->
        val targetLevel = LevelSystem.levelOf(targetId) ?: 1
        val remaining = LevelSystem.remainingToUnlock(targetLevel, savedArtworksMap)
        AlertDialog(
            onDismissRequest = { lockedLevelTarget = null },
            title = {
                Text(
                    text = stringResource(R.string.gallery_level_locked_title, targetLevel),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = pluralStringResource(R.plurals.gallery_level_locked_message, remaining, targetLevel, remaining, targetLevel - 1)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { lockedLevelTarget = null },
                    modifier = Modifier.testTag("btn_dismiss_level_locked_dialog")
                ) {
                    Text(stringResource(R.string.gallery_keep_coloring))
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
                    text = stringResource(R.string.reward_video_unavailable_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.reward_video_unavailable_message, targetDesign.title, 52)
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
                                context.getString(R.string.reward_requesting_video_ad),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkAccent),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.testTag("btn_retry_ad")
                ) {
                    Text(stringResource(R.string.dialog_retry), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { adUnavailableTarget = null },
                    modifier = Modifier.testTag("btn_dismiss_unavailable_dialog")
                ) {
                    Text(stringResource(R.string.reward_explore_free_designs))
                }
            }
        )
    }
}
