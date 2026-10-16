package online.codyuhi.copypastetool.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import online.codyuhi.copypastetool.ui.components.CedarBadge
import online.codyuhi.copypastetool.ui.components.CedarButton
import online.codyuhi.copypastetool.ui.components.CedarCard
import online.codyuhi.copypastetool.ui.components.CedarDangerButton
import online.codyuhi.copypastetool.ui.components.CedarOutlinedButton
import online.codyuhi.copypastetool.ui.components.Lucide
import online.codyuhi.copypastetool.ui.theme.Cedar
import online.codyuhi.copypastetool.ui.theme.MonoSmall
import online.codyuhi.copypastetool.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onSetThemeMode: (ThemeMode) -> Unit,
    onLogin: (user: String, pass: String) -> Unit,
    onRegister: (user: String, pass: String) -> Unit,
    onLogout: () -> Unit,
    onSyncNow: () -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar("Error: $msg")
            onClearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearMessages()
        }
    }

    var authMode by remember { mutableStateOf("login") } // "login" | "register"
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Cedar.colors.canvas,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = Cedar.colors.ink,
                fontWeight = FontWeight.Bold
            )

            // Appearance Section
            CedarCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cedar.colors.ink,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOption(
                        label = "System",
                        isSelected = state.themeMode == ThemeMode.System,
                        onClick = { onSetThemeMode(ThemeMode.System) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        label = "Light",
                        isSelected = state.themeMode == ThemeMode.Light,
                        onClick = { onSetThemeMode(ThemeMode.Light) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOption(
                        label = "Dark",
                        isSelected = state.themeMode == ThemeMode.Dark,
                        onClick = { onSetThemeMode(ThemeMode.Dark) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Account & Cloud Sync Section
            CedarCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Homelab Account & Cloud Sync",
                        style = MaterialTheme.typography.titleMedium,
                        color = Cedar.colors.ink,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!state.authToken.isNullOrBlank()) {
                        CedarBadge(text = "Connected", color = Cedar.colors.ok, bgColor = Cedar.colors.okBg)
                    } else {
                        CedarBadge(text = "Local Only", color = Cedar.colors.warn, bgColor = Cedar.colors.warnBg)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!state.authToken.isNullOrBlank()) {
                    // Logged in state
                    Text(
                        text = "Logged in as ${state.username ?: "User"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Cedar.colors.ink,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val lastSyncStr = state.lastSyncTime?.let {
                        val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                        sdf.format(Date(it))
                    } ?: "Never"

                    Text(
                        text = "Last synced: $lastSyncStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Cedar.colors.ink3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CedarButton(
                            text = "Sync Now",
                            onClick = onSyncNow,
                            leadingIcon = {
                                Icon(imageVector = Lucide.RefreshCw, contentDescription = null, tint = Cedar.colors.onSpruce, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CedarDangerButton(
                            text = "Log Out",
                            onClick = onLogout,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Logged out / offline state: Login or Register form
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOption(
                            label = "Log In",
                            isSelected = authMode == "login",
                            onClick = { authMode = "login" },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOption(
                            label = "Register",
                            isSelected = authMode == "register",
                            onClick = { authMode = "register" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cedar.colors.spruce,
                            unfocusedBorderColor = Cedar.colors.lineStrong,
                            focusedTextColor = Cedar.colors.ink,
                            unfocusedTextColor = Cedar.colors.ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cedar.colors.spruce,
                            unfocusedBorderColor = Cedar.colors.lineStrong,
                            focusedTextColor = Cedar.colors.ink,
                            unfocusedTextColor = Cedar.colors.ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CedarButton(
                        text = if (authMode == "login") "Sign In & Sync" else "Create Account & Sync",
                        enabled = usernameInput.isNotBlank() && passwordInput.isNotBlank(),
                        onClick = {
                            if (authMode == "login") {
                                onLogin(usernameInput.trim(), passwordInput)
                            } else {
                                onRegister(usernameInput.trim(), passwordInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Homelab Info Section
            CedarCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cluster Infrastructure",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cedar.colors.ink,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                InfoRow(label = "Backend URL", value = "https://copy-paste-tool.lan.codyuhi.online")
                Spacer(modifier = Modifier.height(6.dp))
                InfoRow(label = "Scale-to-Zero", value = "Sablier Enabled (cold-start resilient)")
                Spacer(modifier = Modifier.height(6.dp))
                InfoRow(label = "Local Storage", value = "Room DB (copypaste.db)")
                Spacer(modifier = Modifier.height(6.dp))
                InfoRow(label = "App Architecture", value = "Single-Activity MVVM + UDF")
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (isSelected) Cedar.colors.spruce else Cedar.colors.inset
    val textCol = if (isSelected) Cedar.colors.onSpruce else Cedar.colors.ink2

    Surface(
        shape = MaterialTheme.shapes.small,
        color = bg,
        border = BorderStroke(1.dp, if (isSelected) Cedar.colors.spruce else Cedar.colors.line),
        modifier = modifier
            .height(40.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textCol
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Cedar.colors.ink3)
        Text(text = value, style = MonoSmall, color = Cedar.colors.ink)
    }
}
