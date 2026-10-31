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
import androidx.compose.ui.platform.LocalContext
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
    onLanguageChange: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val soundEnabled by appSettings.soundEnabled.collectAsState()
    val hapticEnabled by appSettings.hapticEnabled.collectAsState()
    val outlineColor by appSettings.outlineColor.collectAsState()
    val appLanguage by appSettings.language.collectAsState()
    val effectiveLanguage = appLanguage.ifBlank {
        context.resources.configuration.locales[0].language.takeIf { it == "ar" } ?: "en"
    }

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
        Triple(R.string.settings_outline_classic_black, 0xFF212121L, "classic_black"),
        Triple(R.string.settings_outline_charcoal, 0xFF455A64L, "charcoal"),
        Triple(R.string.settings_outline_deep_indigo, 0xFF1A237EL, "deep_indigo"),
        Triple(R.string.settings_outline_espresso, 0xFF3E2723L, "espresso")
    )

    // Consume Back while an account deletion is running. Leaving this screen
    // would cancel its composition-scoped coroutine after the server lock exists.
    BackHandler {
        if (!cloudBusy) onNavigateBack()
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
                            onClick = { if (!cloudBusy) onNavigateBack() },
                            enabled = !cloudBusy,
                            modifier = Modifier.testTag("btn_settings_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = context.getString(R.string.navigation_back)
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
                // App language is independent from the device-wide language.
                Text(
                    text = stringResource(R.string.settings_language_section),
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(R.string.settings_language_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (effectiveLanguage == "en") {
                                Button(onClick = { onLanguageChange("en") }, modifier = Modifier.weight(1f).testTag("btn_language_english")) { Text(stringResource(R.string.settings_language_english)) }
                            } else {
                                OutlinedButton(onClick = { onLanguageChange("en") }, modifier = Modifier.weight(1f).testTag("btn_language_english")) { Text(stringResource(R.string.settings_language_english)) }
                            }
                            if (effectiveLanguage == "ar") {
                                Button(onClick = { onLanguageChange("ar") }, modifier = Modifier.weight(1f).testTag("btn_language_arabic")) { Text(stringResource(R.string.settings_language_arabic)) }
                            } else {
                                OutlinedButton(onClick = { onLanguageChange("ar") }, modifier = Modifier.weight(1f).testTag("btn_language_arabic")) { Text(stringResource(R.string.settings_language_arabic)) }
                            }
                        }
                    }
                }

                // Audio & Haptics Section
                Text(
                    text = stringResource(R.string.settings_feedback_section),
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
                                        text = stringResource(R.string.settings_sound_description),
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
                                        text = stringResource(R.string.settings_haptics_description),
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
                    text = stringResource(R.string.settings_outline_section),
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
                            text = stringResource(R.string.settings_outline_description),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            outlineColorOptions.forEach { (labelRes, colorLong, testTagSuffix) ->
                                val isSelected = outlineColor == colorLong
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { appSettings.setOutlineColor(colorLong) }
                                        .padding(8.dp)
                                        .testTag("outline_opt_$testTagSuffix")
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
                                        text = stringResource(labelRes),
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
                    text = stringResource(R.string.settings_privacy_legal_section),
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
                            Text(stringResource(R.string.settings_privacy_policy), fontWeight = FontWeight.SemiBold)
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
                            Text(stringResource(R.string.settings_licenses), fontWeight = FontWeight.SemiBold)
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
                                Text(stringResource(R.string.settings_manage_consent), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Optional cloud save section. Local Room saves remain available without an account.
                if (cloudSaveManager != null) {
                    Text(
                        text = stringResource(R.string.settings_cloud_save),
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
                                Text(stringResource(R.string.settings_signed_in_as, signedInUser.email ?: context.getString(R.string.settings_firebase_user)))
                                Text(
                                    stringResource(R.string.settings_cloud_sync_description),
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
                                                context.getString(R.string.settings_cloud_sync_completed)
                                            } catch (error: Exception) {
                                                error.localizedMessage ?: context.getString(R.string.settings_cloud_sync_failed)
                                            }
                                            cloudBusy = false
                                        }
                                    },
                                    enabled = !cloudBusy,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_sync")
                                ) { Text(if (cloudBusy) stringResource(R.string.settings_syncing) else stringResource(R.string.settings_sync_progress)) }
                                Text(
                                    stringResource(R.string.settings_import_description),
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
                                                context.getString(R.string.settings_import_completed)
                                            } catch (error: Exception) {
                                                error.localizedMessage ?: context.getString(R.string.settings_import_failed)
                                            }
                                            cloudBusy = false
                                        }
                                    },
                                    enabled = !cloudBusy,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_import_local")
                                ) { Text(if (cloudBusy) stringResource(R.string.settings_importing) else stringResource(R.string.settings_import_local)) }
                                OutlinedButton(
                                    onClick = {
                                        cloudSaveManager.signOut()
                                        cloudMessage = context.getString(R.string.settings_signed_out)
                                    },
                                    enabled = !cloudBusy,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_sign_out")
                                ) { Text(stringResource(R.string.settings_sign_out)) }
                                OutlinedButton(
                                    onClick = { showDeleteAccountDialog = true },
                                    enabled = !cloudBusy,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_delete_account")
                                ) {
                                    Text(stringResource(R.string.settings_delete_account), color = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                Text(
                                    stringResource(R.string.settings_cloud_sign_in_description),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = cloudEmail,
                                    onValueChange = { cloudEmail = it },
                                    label = { Text(stringResource(R.string.settings_email)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("field_cloud_email")
                                )
                                OutlinedTextField(
                                    value = cloudPassword,
                                    onValueChange = { cloudPassword = it },
                                    label = { Text(stringResource(R.string.settings_password_hint)) },
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
                                    Text(if (createCloudAccount) stringResource(R.string.settings_create_new_account) else stringResource(R.string.settings_existing_account))
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
                                                context.getString(R.string.settings_sign_in_completed)
                                            } catch (error: Exception) {
                                                error.localizedMessage ?: context.getString(R.string.settings_sign_in_failed)
                                            }
                                            cloudBusy = false
                                        }
                                    },
                                    enabled = !cloudBusy && cloudEmail.contains("@") && cloudPassword.length >= 6,
                                    modifier = Modifier.fillMaxWidth().testTag("btn_cloud_auth")
                                ) {
                                    Text(if (cloudBusy) stringResource(R.string.settings_please_wait) else if (createCloudAccount) stringResource(R.string.settings_create_account_sync) else stringResource(R.string.settings_sign_in_sync))
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
                                    text = stringResource(R.string.app_name),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
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
                            text = stringResource(R.string.settings_feature_summary),
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
                Text(stringResource(R.string.settings_privacy_policy), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.settings_privacy_policy_body),
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
                    Text(stringResource(R.string.dialog_close))
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
                Text(stringResource(R.string.settings_delete_account), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.settings_delete_account_confirmation)
                    )
                    OutlinedTextField(
                        value = deleteAccountPassword,
                        onValueChange = { deleteAccountPassword = it },
                        label = { Text(stringResource(R.string.settings_account_password)) },
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
                        cloudBusy = true
                        showDeleteAccountDialog = false
                        cloudScope.launch {
                            cloudMessage = null
                            cloudMessage = try {
                                cloudSaveManager?.deleteAccount(deleteAccountPassword)
                                context.getString(R.string.settings_account_delete_completed)
                            } catch (error: Exception) {
                                error.localizedMessage ?: context.getString(R.string.settings_account_delete_failed)
                            } finally {
                                deleteAccountPassword = ""
                                cloudBusy = false
                            }
                        }
                    },
                    enabled = !cloudBusy && deleteAccountPassword.isNotBlank()
                ) {
                    Text(stringResource(R.string.settings_delete_permanently), color = MaterialTheme.colorScheme.error)
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
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // In-App Open Source Licenses & Copyright Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = {
                Text(stringResource(R.string.settings_licenses), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.settings_licenses_body),
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
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }
}
