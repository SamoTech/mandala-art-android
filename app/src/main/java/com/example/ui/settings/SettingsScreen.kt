package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.CloudSaveManager
import kotlinx.coroutines.launch
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurplePrimary
import com.example.util.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appSettings: AppSettings,
    onManageConsent: (() -> Unit)? = null,
    cloudSaveManager: CloudSaveManager? = null,
    onNavigateBack: () -> Unit
) {
    val soundEnabled by appSettings.soundEnabled.collectAsState()
    val hapticEnabled by appSettings.hapticEnabled.collectAsState()
    val outlineColor by appSettings.outlineColor.collectAsState()

    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteAccountPassword by remember { mutableStateOf("") }
    var cloudEmail by remember { mutableStateOf("") }
    var cloudPassword by remember { mutableStateOf("") }
    var createCloudAccount by remember { mutableStateOf(false) }
    var cloudBusy by remember { mutableStateOf(false) }
    var cloudMessage by remember { mutableStateOf<String?>(null) }
    val cloudScope = rememberCoroutineScope()

    val outlineColorOptions = listOf(
        Pair("Classic Black", 0xFF212121L),
        Pair("Charcoal", 0xFF455A64L),
        Pair("Deep Indigo", 0xFF1A237EL),
        Pair("Espresso", 0xFF3E2723L)
    )

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_root"),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("btn_settings_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 650.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Audio & Haptics Section
                Text(
                    text = "Feedback & Experience",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        // Sound Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_sound),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Subtle click on region coloring",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { appSettings.setSoundEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PinkAccent,
                                    checkedTrackColor = PinkAccent.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("switch_sound")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Haptics Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.settings_haptics),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Gentle tactile pulse on tap",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = hapticEnabled,
                                onCheckedChange = { appSettings.setHapticEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PinkAccent,
                                    checkedTrackColor = PinkAccent.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("switch_haptic")
                            )
                        }
                    }
                }

                // Outline Style Section
                Text(
                    text = "Artwork Outlines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = "Choose the line stroke color for your mandalas:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            outlineColorOptions.forEach { (label, colorLong) ->
                                val isSelected = outlineColor == colorLong
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { appSettings.setOutlineColor(colorLong) }
                                        .padding(8.dp)
                                        .testTag("outline_opt_${label.lowercase().replace(' ', '_')}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(colorLong))
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) PinkAccent else Color.LightGray,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Privacy & Legal Compliance Section (Google Play / AdMob UMP Requirement)
                Text(
                    text = "Privacy & Legal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // In-app Privacy Policy dialog trigger
                        OutlinedButton(
                            onClick = { showPrivacyPolicyDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_privacy_policy"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Policy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Privacy Policy & Data Safety", fontWeight = FontWeight.SemiBold)
                        }

                        // Open Source Licenses & Copyright Notice
                        OutlinedButton(
                            onClick = { showLicensesDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_licenses"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Licenses & Attribution", fontWeight = FontWeight.SemiBold)
                        }

                        // UMP Consent Management button
                        if (onManageConsent != null) {
                            OutlinedButton(
                                onClick = onManageConsent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_manage_consent"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manage Ad & Privacy Consent", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Optional cloud save section. Local Room saves remain available without an account.
                if (cloudSaveManager != null) {
                    Text(
                        text = "Cloud Save",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val signedInUser = cloudSaveManager.currentUser
                            if (signedInUser != null) {
                                Text("Signed in as ${signedInUser.email ?: "Firebase user"}")
                                Text(
                                    "Your local progress stays available offline. Sync merges coloring, favorites, and deletions independently across devices.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        cloudScope.launch {
                                            cloudBusy = true
                                            cloudMessage = null
                                            cloudMessage = try {
                                                cloudSaveManager.syncProgress()
                                                "Cloud sync completed."
                                            } catch (error: Exception) {
                                                error.localizedMessage ?: "Cloud sync failed. Check your connection and Firestore rules."
                                            }
                                            cloudBusy = false
                                        }
                                    },
                                    enabled = !cloudBusy,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_sync")
                                ) { Text(if (cloudBusy) "Syncing..." else "Sync Progress") }
                                Text(
                                    "This is an optional one-time merge of local-only saves into this account. The original local copy is retained. Use it only when the local work belongs to you.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = {
                                        cloudScope.launch {
                                            cloudBusy = true
                                            cloudMessage = null
                                            cloudMessage = try {
                                                cloudSaveManager.importAnonymousProgress()
                                                "Local progress imported; the local copy remains available."
                                            } catch (error: Exception) {
                                                error.localizedMessage ?: "Import failed."
                                            }
                                            cloudBusy = false
                                        }
                                    },
                                    enabled = !cloudBusy,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_import_local")
                                ) { Text(if (cloudBusy) "Importing..." else "Import Local Progress") }
                                OutlinedButton(
                                    onClick = {
                                        cloudSaveManager.signOut()
                                        cloudMessage = "Signed out. Local progress remains on this device."
                                    },
                                    enabled = !cloudBusy,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_sign_out")
                                ) { Text("Sign Out") }
                                OutlinedButton(
                                    onClick = { showDeleteAccountDialog = true },
                                    enabled = !cloudBusy,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_delete_account")
                                ) {
                                    Text("Delete Account & Cloud Data", color = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                Text(
                                    "Create an account or sign in to back up your coloring progress across devices.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = cloudEmail,
                                    onValueChange = { cloudEmail = it },
                                    label = { Text("Email") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("field_cloud_email")
                                )
                                OutlinedTextField(
                                    value = cloudPassword,
                                    onValueChange = { cloudPassword = it },
                                    label = { Text("Password (at least 6 characters)") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().testTag("field_cloud_password")
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = createCloudAccount,
                                        onCheckedChange = { createCloudAccount = it },
                                        enabled = !cloudBusy
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (createCloudAccount) "Create new account" else "I already have an account")
                                }
                                Button(
                                    onClick = {
                                        cloudScope.launch {
                                            cloudBusy = true
                                            cloudMessage = null
                                            cloudMessage = try {
                                                if (createCloudAccount) {
                                                    cloudSaveManager.createAccount(cloudEmail, cloudPassword)
                                                } else {
                                                    cloudSaveManager.signIn(cloudEmail, cloudPassword)
                                                }
                                                cloudPassword = ""
                                                "Signed in. Cloud sync completed."
                                            } catch (error: Exception) {
                                                error.localizedMessage ?: "Could not sign in. Check your details and connection."
                                            }
                                            cloudBusy = false
                                        }
                                    },
                                    enabled = !cloudBusy && cloudEmail.contains("@") && cloudPassword.length >= 6,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_auth")
                                ) {
                                    Text(if (cloudBusy) "Please wait..." else if (createCloudAccount) "Create Account & Sync" else "Sign In & Sync")
                                }
                            }
                            cloudMessage?.let {
                                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // About Mandala Art Section
                Text(
                    text = stringResource(R.string.settings_about),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(PurplePrimary, PinkAccent))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Mandala Art",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Version ${BuildConfig.VERSION_NAME} • Native Android",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.settings_about_desc),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• 104 original algorithmic vector designs\n• Offline-first local saves with optional cloud backup\n• 5 relaxing curated color themes\n• Unlimited Undo & Redo with zoom & pan",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // In-App Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text("Privacy Policy & Data Safety", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "1. Optional Cloud Save:\n" +
                                "Account creation is optional. If you create an account, Firebase Authentication processes your email address. When you manually sync, your coloring progress, colors, favorites, completion status, timestamps, per-field sync revisions, and deletion markers are stored in Cloud Firestore under your account. A random installation identifier is used only to resolve concurrent offline edits; it is stored outside Android backup data. Without signing in, progress remains local.\n\n" +
                                "2. Advertising & Third-Party SDKs:\n" +
                                "The app uses Google AdMob (Google Mobile Ads SDK) to show non-intrusive ads and voluntary rewarded video unlocks. Google AdMob may process device identifiers (such as Android Advertising ID), coarse IP address, and interaction diagnostics for ad serving and fraud detection.\n\n" +
                                "The app also uses Firebase Analytics with a small, fixed set of app events: when a design is opened, when coloring is started, when a rewarded ad is offered, started, or completed, and when premium content is unlocked. These events include only a coarse content category, never artwork images, colors, or coloring progress. Firebase also collects an app instance identifier and standard app lifecycle and technical information.\n\n" +
                                "3. User Consent (GDPR / CCPA):\n" +
                                "In regions requiring consent (such as the European Economic Area and UK), Google User Messaging Platform (UMP) requests your consent for personalized ads before ads are requested. You can update your consent at any time in Settings.\n\n" +
                                "4. Children's Privacy:\n" +
                                "We treat ads as General Audience (Content Rating G) and do not knowingly collect personal information from children under 13.\n\n" +
                                "5. Data Deletion:\n" +
                                "Local progress can be deleted using the app controls or Android App Settings. Signing out does not delete cloud saves. Contact the project maintainers to request deletion of cloud account data.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkAccent)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Account deletion requires explicit confirmation and recent authentication.
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!cloudBusy) {
                    showDeleteAccountDialog = false
                    deleteAccountPassword = ""
                }
            },
            title = {
                Text("Delete Account & Cloud Data", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "This permanently deletes your Firebase account, its cloud saves, and this account's local progress. Guest progress and other accounts on this device are kept. Enter your password to reauthenticate."
                    )
                    OutlinedTextField(
                        value = deleteAccountPassword,
                        onValueChange = { deleteAccountPassword = it },
                        label = { Text("Account password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !cloudBusy,
                        modifier = Modifier.fillMaxWidth().testTag("field_delete_account_password")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        cloudScope.launch {
                            cloudBusy = true
                            cloudMessage = null
                            cloudMessage = try {
                                cloudSaveManager?.deleteAccount(deleteAccountPassword)
                                "Account deletion completed."
                            } catch (error: Exception) {
                                error.localizedMessage ?: "Account deletion failed. You can retry from this account."
                            } finally {
                                deleteAccountPassword = ""
                                cloudBusy = false
                            }
                        }
                    },
                    enabled = !cloudBusy && deleteAccountPassword.isNotBlank()
                ) {
                    Text("Delete permanently", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccountPassword = ""
                    },
                    enabled = !cloudBusy
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // In-App Open Source Licenses & Copyright Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = {
                Text("Licenses & Attribution", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "1. Mandala Art Original Artwork:\n" +
                                "All 104 mandala artwork patterns and line-art designs in this application are original algorithmic vector works mathematically generated for Mandala Art. Copyright (c) 2026 Mandala Art Project.\n\n" +
                                "2. Android Jetpack & Libraries:\n" +
                                "Licensed under the Apache License, Version 2.0 (the 'License').\n" +
                                "Includes:\n" +
                                "• AndroidX Core, Activity, Lifecycle, Compose (M3)\n" +
                                "• AndroidX Room Persistence Library\n" +
                                "• Kotlin Standard Library and Coroutines\n\n" +
                                "3. Google Mobile Ads SDK:\n" +
                                "Provided by Google LLC under Google APIs Terms of Service.\n\n" +
                                "You may obtain a copy of the Apache License at:\n" +
                                "http://www.apache.org/licenses/LICENSE-2.0",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLicensesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkAccent)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
