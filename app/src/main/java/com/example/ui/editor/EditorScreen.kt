package com.example.ui.editor

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurplePrimary
import com.example.util.AppSettings
import com.example.ads.AdMobManager
import com.example.analytics.AnalyticsTracker
import androidx.compose.runtime.LaunchedEffect
import com.example.util.ImageSharer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    appSettings: AppSettings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val shareScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val navigationScope = rememberCoroutineScope()
    var isNavigatingBack by remember { mutableStateOf(false) }
    val navigateBackAfterFlush: () -> Unit = {
        if (!isNavigatingBack) {
            isNavigatingBack = true
            navigationScope.launch {
                try {
                    viewModel.flushPendingSave()
                    onNavigateBack()
                } catch (cancelled: CancellationException) {
                    isNavigatingBack = false
                    throw cancelled
                } catch (error: Exception) {
                    isNavigatingBack = false
                    Log.e("EditorScreen", "Unable to flush artwork before navigation", error)
                    // The retained autosave error state is rendered as an actionable dialog.
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = EditorLifecycleSaveObserver(lifecycleOwner.lifecycle) {
            viewModel.flushPendingSaveOnLifecycleStop { error ->
                Log.e("EditorScreen", "Autosave flush failed when app stopped", error)
            }
        }
        onDispose { observer.close() }
    }

    LaunchedEffect(viewModel.design.id) {
        AnalyticsTracker.logEvent("design_opened", "content_type", "mandala")
        AnalyticsTracker.logEvent("coloring_started", "content_type", "mandala")
    }
    val coloredRegions by viewModel.coloredRegions.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val isEraserActive by viewModel.isEraserActive.collectAsState()
    val isZoomPanMode by viewModel.isZoomPanMode.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val percentCompleted by viewModel.percentCompleted.collectAsState()
    val hasAutosaveError by viewModel.hasAutosaveError.collectAsState()
    val autosaveErrorVersion by viewModel.autosaveErrorVersion.collectAsState()
    val outlineColor by appSettings.outlineColor.collectAsState()

    var showAutosaveErrorDialog by remember { mutableStateOf(false) }
    var isAutosaveRetryInProgress by remember { mutableStateOf(false) }
    LaunchedEffect(autosaveErrorVersion, hasAutosaveError) {
        showAutosaveErrorDialog = hasAutosaveError
        if (!hasAutosaveError) isAutosaveRetryInProgress = false
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var lockedColorToUnlock by remember { mutableStateOf<Long?>(null) }
    var colorAdInProgress by remember { mutableStateOf(false) }

    BackHandler(enabled = !isNavigatingBack) {
        navigateBackAfterFlush()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("editor_screen_root"),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = navigateBackAfterFlush,
                            modifier = Modifier.testTag("btn_editor_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.navigation_back)
                            )
                        }

                        // Title & Progress Badge
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = viewModel.design.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(
                                    R.string.editor_progress_summary,
                                    (percentCompleted * 100).toInt(),
                                    pluralStringResource(
                                        R.plurals.editor_region_count,
                                        viewModel.design.regionCount,
                                        viewModel.design.regionCount
                                    )
                                ),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Top right quick actions: Undo, Redo, Reset, Share
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.undo() },
                                enabled = canUndo,
                                modifier = Modifier.testTag("btn_editor_undo")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = stringResource(R.string.editor_undo),
                                    tint = if (canUndo) MaterialTheme.colorScheme.primary else Color.LightGray
                                )
                            }

                            IconButton(
                                onClick = { viewModel.redo() },
                                enabled = canRedo,
                                modifier = Modifier.testTag("btn_editor_redo")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Redo,
                                    contentDescription = stringResource(R.string.editor_redo),
                                    tint = if (canRedo) MaterialTheme.colorScheme.primary else Color.LightGray
                                )
                            }

                            IconButton(
                                onClick = { showResetDialog = true },
                                modifier = Modifier.testTag("btn_editor_reset")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = stringResource(R.string.editor_reset),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }

                            IconButton(
                                onClick = {
                                    shareScope.launch {
                                        try {
                                            ImageSharer.exportAndShare(
                                                context = context,
                                                design = viewModel.design,
                                                coloredRegions = coloredRegions,
                                                outlineColor = outlineColor
                                            )
                                        } catch (cancelled: CancellationException) {
                                            throw cancelled
                                        } catch (error: Exception) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.editor_export_error),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("btn_editor_share")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = stringResource(R.string.editor_share),
                                    tint = PinkAccent
                                )
                            }
                        }
                    }

                    // Slim completion indicator bar
                    LinearProgressIndicator(
                        progress = { percentCompleted },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = PinkAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        bottomBar = {
            ColorPaletteBar(
                selectedColor = selectedColor,
                isEraserActive = isEraserActive,
                isZoomPanMode = isZoomPanMode,
                onColorSelected = { color ->
                    viewModel.setSelectedColor(color)
                },
                onToggleEraser = {
                    viewModel.toggleEraser()
                },
                onToggleZoomPan = {
                    viewModel.toggleZoomPanMode()
                },
                onOpenColorPicker = {
                    showColorPicker = true
                },
                isColorUnlocked = { color -> appSettings.isColorUnlocked(color) },
                onRequestUnlockColor = { color -> lockedColorToUnlock = color },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 650.dp),
                contentAlignment = Alignment.Center
            ) {
                MandalaCanvas(
                    design = viewModel.design,
                    coloredRegions = coloredRegions,
                    outlineColor = outlineColor,
                    isZoomPanMode = isZoomPanMode,
                    onRegionTapped = { regionId ->
                        viewModel.onRegionTapped(regionId, context)
                    }
                )

                // Mode helper pill badge
                if (isZoomPanMode) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = PurplePrimary.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = stringResource(R.string.editor_zoom_pan_hint),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                } else if (isEraserActive) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = PinkAccent.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = stringResource(R.string.editor_eraser_hint),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    // Keep background autosave failures visible when the editor is in the foreground.
    if (showAutosaveErrorDialog && hasAutosaveError) {
        AlertDialog(
            onDismissRequest = {
                if (!isAutosaveRetryInProgress) showAutosaveErrorDialog = false
            },
            title = {
                Text(
                    text = stringResource(R.string.autosave_error_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(stringResource(R.string.autosave_error_message)) },
            confirmButton = {
                Button(
                    enabled = !isAutosaveRetryInProgress,
                    onClick = {
                        if (!isAutosaveRetryInProgress) {
                            isAutosaveRetryInProgress = true
                            navigationScope.launch {
                                try {
                                    viewModel.flushPendingSave()
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (error: Exception) {
                                    Log.e("EditorScreen", "Autosave retry failed", error)
                                } finally {
                                    isAutosaveRetryInProgress = false
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        if (isAutosaveRetryInProgress) {
                            stringResource(R.string.autosave_retrying)
                        } else {
                            stringResource(R.string.autosave_retry)
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isAutosaveRetryInProgress,
                    onClick = { showAutosaveErrorDialog = false }
                ) {
                    Text(stringResource(R.string.autosave_keep_editing))
                }
            }
        )
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.editor_reset_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.editor_reset_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        navigationScope.launch {
                            try {
                                viewModel.reset()
                                showResetDialog = false
                                Toast.makeText(context, context.getString(R.string.editor_reset_success), Toast.LENGTH_SHORT).show()
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (error: Exception) {
                                Log.e("EditorScreen", "Unable to reset artwork", error)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.editor_reset_error),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_reset")
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    modifier = Modifier.testTag("btn_cancel_reset")
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }


    lockedColorToUnlock?.let { color ->
        LaunchedEffect(color) {
            AnalyticsTracker.logEvent("rewarded_ad_offered", "content_type", "color")
        }
        AlertDialog(
            onDismissRequest = { if (!colorAdInProgress) lockedColorToUnlock = null },
            title = { Text(stringResource(R.string.editor_unlock_premium_color_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.editor_unlock_premium_color_message)) },
            confirmButton = {
                Button(enabled = !colorAdInProgress, onClick = {
                    val activity = context as? android.app.Activity
                    if (activity == null) {
                        Toast.makeText(context, context.getString(R.string.editor_ad_show_error), Toast.LENGTH_SHORT).show()
                    } else {
                        colorAdInProgress = true
                        AnalyticsTracker.logEvent("rewarded_ad_started", "content_type", "color")
                        AdMobManager.getInstance(context).showRewardedAd(
                            activity = activity,
                            onUserEarnedReward = { _ ->
                                AnalyticsTracker.logEvent("rewarded_ad_completed", "content_type", "color")
                                appSettings.unlockColor(color)
                                AnalyticsTracker.logEvent("premium_unlock_granted", "content_type", "color")
                                viewModel.setSelectedColor(color)
                                lockedColorToUnlock = null
                            },
                            onAdClosed = { earned ->
                                colorAdInProgress = false
                                if (!earned) Toast.makeText(context, context.getString(R.string.editor_ad_not_completed), Toast.LENGTH_SHORT).show()
                            },
                            onAdUnavailable = {
                                colorAdInProgress = false
                                Toast.makeText(context, context.getString(R.string.editor_ad_not_ready), Toast.LENGTH_LONG).show()
                            },
                            onAdFailedToShow = { _ ->
                                colorAdInProgress = false
                                Toast.makeText(context, context.getString(R.string.editor_ad_failed), Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = PinkAccent)) { Text(stringResource(R.string.editor_watch_video)) }
            },
            dismissButton = { TextButton(onClick = { if (!colorAdInProgress) lockedColorToUnlock = null }) { Text(stringResource(R.string.editor_cancel)) } }
        )
    }

    // Custom Color Picker Dialog
    if (showColorPicker) {
        CustomColorPickerDialog(
            initialColor = selectedColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { newColor ->
                viewModel.setSelectedColor(newColor)
                showColorPicker = false
            }
        )
    }
}
