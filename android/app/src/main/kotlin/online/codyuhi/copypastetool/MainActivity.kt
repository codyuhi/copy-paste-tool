package online.codyuhi.copypastetool

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import online.codyuhi.copypastetool.ui.AppScaffold
import online.codyuhi.copypastetool.ui.favorites.FavoritesViewModel
import online.codyuhi.copypastetool.ui.sections.SectionsViewModel
import online.codyuhi.copypastetool.ui.settings.SettingsViewModel
import online.codyuhi.copypastetool.ui.snippets.SnippetsViewModel
import online.codyuhi.copypastetool.ui.theme.CedarDark
import online.codyuhi.copypastetool.ui.theme.CedarLight
import online.codyuhi.copypastetool.ui.theme.CedarTheme
import online.codyuhi.copypastetool.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    private var sharedText: String? by mutableStateOf(null)
    private var deepLinkRoute: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        val app = application as CopyPasteApplication
        val container = app.container

        handleIntent(intent)

        setContent {
            val themeMode by container.settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.System)

            val systemDark = isSystemInDarkTheme()
            val dark = when (themeMode) {
                ThemeMode.System -> systemDark
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            LaunchedEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            var lanGranted by remember { mutableStateOf(hasLocalNetworkAccess()) }
            val lanPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                lanGranted = granted
            }

            LifecycleResumeEffect(Unit) {
                lanGranted = hasLocalNetworkAccess()
                onPauseOrDispose { }
            }

            val snippetsVm: SnippetsViewModel = viewModel(
                factory = viewModelFactory { initializer { SnippetsViewModel(container.copyPasteRepository) } }
            )
            val favoritesVm: FavoritesViewModel = viewModel(
                factory = viewModelFactory { initializer { FavoritesViewModel(container.copyPasteRepository) } }
            )
            val sectionsVm: SectionsViewModel = viewModel(
                factory = viewModelFactory { initializer { SectionsViewModel(container.copyPasteRepository) } }
            )
            val settingsVm: SettingsViewModel = viewModel(
                factory = viewModelFactory { initializer { SettingsViewModel(container.settingsRepository, container.copyPasteRepository) } }
            )

            val colors = if (dark) CedarDark else CedarLight
            CedarTheme(colors = colors) {
                AppScaffold(
                    snippetsViewModel = snippetsVm,
                    favoritesViewModel = favoritesVm,
                    sectionsViewModel = sectionsVm,
                    settingsViewModel = settingsVm,
                    currentThemeMode = themeMode,
                    isDarkTheme = dark,
                    onToggleTheme = {
                        val nextMode = when (themeMode) {
                            ThemeMode.System -> if (systemDark) ThemeMode.Light else ThemeMode.Dark
                            ThemeMode.Light -> ThemeMode.Dark
                            ThemeMode.Dark -> ThemeMode.Light
                        }
                        settingsVm.setThemeMode(nextMode)
                    },
                    sharedTextIntent = sharedText,
                    deepLinkRoute = deepLinkRoute,
                    hasLocalNetworkAccess = lanGranted,
                    onRequestLocalNetwork = {
                        if (Build.VERSION.SDK_INT >= 37) {
                            lanPermissionLauncher.launch("android.permission.ACCESS_LOCAL_NETWORK")
                        } else {
                            openAppSettings()
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                }
            }
            Intent.ACTION_VIEW -> {
                val data = intent.data
                if (data != null && data.scheme == "copypaste") {
                    deepLinkRoute = data.host
                }
            }
        }
    }

    private fun hasLocalNetworkAccess(): Boolean {
        if (Build.VERSION.SDK_INT < 37) return true
        return ContextCompat.checkSelfPermission(
            this,
            "android.permission.ACCESS_LOCAL_NETWORK"
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}
