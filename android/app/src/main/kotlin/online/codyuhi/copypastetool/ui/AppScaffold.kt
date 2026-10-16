package online.codyuhi.copypastetool.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import online.codyuhi.copypastetool.data.repository.SyncStatus
import online.codyuhi.copypastetool.ui.components.AddEditSnippetDialog
import online.codyuhi.copypastetool.ui.components.CedarThemeToggle
import online.codyuhi.copypastetool.ui.components.LocalNetworkBanner
import online.codyuhi.copypastetool.ui.components.Lucide
import online.codyuhi.copypastetool.ui.favorites.FavoritesScreen
import online.codyuhi.copypastetool.ui.favorites.FavoritesViewModel
import online.codyuhi.copypastetool.ui.navigation.NavDestination
import online.codyuhi.copypastetool.ui.sections.SectionsScreen
import online.codyuhi.copypastetool.ui.sections.SectionsViewModel
import online.codyuhi.copypastetool.ui.settings.SettingsScreen
import online.codyuhi.copypastetool.ui.settings.SettingsViewModel
import online.codyuhi.copypastetool.ui.snippets.SnippetsScreen
import online.codyuhi.copypastetool.ui.snippets.SnippetsViewModel
import online.codyuhi.copypastetool.ui.theme.Cedar
import online.codyuhi.copypastetool.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    snippetsViewModel: SnippetsViewModel,
    favoritesViewModel: FavoritesViewModel,
    sectionsViewModel: SectionsViewModel,
    settingsViewModel: SettingsViewModel,
    currentThemeMode: ThemeMode,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    sharedTextIntent: String?,
    deepLinkRoute: String?,
    hasLocalNetworkAccess: Boolean,
    onRequestLocalNetwork: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Snippets) }

    // Handle deep links
    LaunchedEffect(deepLinkRoute) {
        when (deepLinkRoute) {
            "favorites" -> currentDestination = NavDestination.Favorites
            "search" -> currentDestination = NavDestination.Snippets
            "new_snippet" -> currentDestination = NavDestination.Snippets
        }
    }

    // Handle incoming shared text
    var incomingShareText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(sharedTextIntent) {
        if (!sharedTextIntent.isNullOrBlank()) {
            incomingShareText = sharedTextIntent
            currentDestination = NavDestination.Snippets
        }
    }

    val destinations = listOf(
        NavDestination.Snippets,
        NavDestination.Favorites,
        NavDestination.Sections,
        NavDestination.Settings
    )

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val widthClass = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass

    // Responsive navigation suite: slim rail on Medium/Expanded (Galaxy Z Fold unfolded 752dp), bottom bar on Compact
    val navSuiteType = if (widthClass == WindowWidthSizeClass.COMPACT) {
        NavigationSuiteType.NavigationBar
    } else {
        NavigationSuiteType.NavigationRail
    }

    val snippetsUiState by snippetsViewModel.uiState.collectAsState()
    val favoritesUiState by favoritesViewModel.uiState.collectAsState()
    val sectionsUiState by sectionsViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    NavigationSuiteScaffold(
        layoutType = navSuiteType,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = Cedar.colors.surface,
            navigationBarContentColor = Cedar.colors.ink2,
            navigationRailContainerColor = Cedar.colors.surface,
            navigationRailContentColor = Cedar.colors.ink2,
        ),
        navigationSuiteItems = {
            destinations.forEach { dest ->
                val selected = currentDestination == dest
                item(
                    selected = selected,
                    onClick = { currentDestination = dest },
                    icon = {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.title,
                            tint = if (selected) Cedar.colors.spruce else Cedar.colors.ink3,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = dest.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Cedar.colors.spruce else Cedar.colors.ink3,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Copy-Paste Tool",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Cedar.colors.ink
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Cloud status icon indicator
                            if (!settingsUiState.authToken.isNullOrBlank()) {
                                when (snippetsUiState.syncStatus) {
                                    is SyncStatus.Syncing -> {
                                        Icon(
                                            imageVector = Lucide.RefreshCw,
                                            contentDescription = "Syncing",
                                            tint = Cedar.colors.brand,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    is SyncStatus.Success -> {
                                        Icon(
                                            imageVector = Lucide.Cloud,
                                            contentDescription = "Connected",
                                            tint = Cedar.colors.ok,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    is SyncStatus.Error -> {
                                        Icon(
                                            imageVector = Lucide.CloudOff,
                                            contentDescription = "Sync Error",
                                            tint = Cedar.colors.rust,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Lucide.Cloud,
                                            contentDescription = "Cloud Connected",
                                            tint = Cedar.colors.spruce,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        CedarThemeToggle(
                            currentMode = currentThemeMode,
                            isDark = isDarkTheme,
                            onToggle = onToggleTheme,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Cedar.colors.surface,
                        titleContentColor = Cedar.colors.ink
                    )
                )
            },
            containerColor = Cedar.colors.canvas
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Local network permission banner (Android 17+ on home Wi-Fi)
                if (!hasLocalNetworkAccess && android.os.Build.VERSION.SDK_INT >= 37) {
                    LocalNetworkBanner(
                        onRequestAccess = onRequestLocalNetwork,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }

                when (currentDestination) {
                    NavDestination.Snippets -> {
                        SnippetsScreen(
                            state = snippetsUiState,
                            onSearchChange = snippetsViewModel::onSearchQueryChange,
                            onSectionFilterChange = snippetsViewModel::onSectionFilterChange,
                            onCopy = snippetsViewModel::onSnippetCopied,
                            onToggleFavorite = snippetsViewModel::toggleFavorite,
                            onAddSnippet = snippetsViewModel::addSnippet,
                            onUpdateSnippet = snippetsViewModel::updateSnippet,
                            onDeleteSnippet = snippetsViewModel::deleteSnippet,
                            onAddSection = snippetsViewModel::addSection,
                            onDeleteSection = snippetsViewModel::deleteSection,
                            onClearToast = snippetsViewModel::clearToast,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    NavDestination.Favorites -> {
                        FavoritesScreen(
                            state = favoritesUiState,
                            onSearchChange = favoritesViewModel::onSearchQueryChange,
                            onCopy = favoritesViewModel::onSnippetCopied,
                            onToggleFavorite = favoritesViewModel::toggleFavorite,
                            onUpdateSnippet = favoritesViewModel::updateSnippet,
                            onDeleteSnippet = favoritesViewModel::deleteSnippet,
                            onClearToast = favoritesViewModel::clearToast,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    NavDestination.Sections -> {
                        SectionsScreen(
                            state = sectionsUiState,
                            onAddSection = sectionsViewModel::addSection,
                            onRenameSection = sectionsViewModel::renameSection,
                            onDeleteSection = sectionsViewModel::deleteSection,
                            onClearToast = sectionsViewModel::clearToast,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    NavDestination.Settings -> {
                        SettingsScreen(
                            state = settingsUiState,
                            onSetThemeMode = settingsViewModel::setThemeMode,
                            onLogin = settingsViewModel::login,
                            onRegister = settingsViewModel::register,
                            onLogout = settingsViewModel::logout,
                            onSyncNow = settingsViewModel::syncNow,
                            onClearMessages = settingsViewModel::clearMessages,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Incoming Share Sheet Dialog: Save shared text as a new snippet
                incomingShareText?.let { text ->
                    AddEditSnippetDialog(
                        initialName = if (text.length > 25) text.take(25) + "…" else text,
                        initialValue = text,
                        availableSections = snippetsUiState.allSections,
                        onDismiss = { incomingShareText = null },
                        onSave = { name, value, secId, isFav ->
                            incomingShareText = null
                            snippetsViewModel.addSnippet(name, value, secId, isFav)
                        }
                    )
                }
            }
        }
    }
}
