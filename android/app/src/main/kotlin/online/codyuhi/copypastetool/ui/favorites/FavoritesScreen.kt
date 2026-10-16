package online.codyuhi.copypastetool.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import online.codyuhi.copypastetool.data.db.ButtonEntity
import online.codyuhi.copypastetool.ui.components.AddEditSnippetDialog
import online.codyuhi.copypastetool.ui.components.DeleteConfirmationDialog
import online.codyuhi.copypastetool.ui.components.Lucide
import online.codyuhi.copypastetool.ui.components.SnippetCard
import online.codyuhi.copypastetool.ui.theme.Cedar

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onSearchChange: (String) -> Unit,
    onCopy: (String) -> Unit,
    onToggleFavorite: (buttonId: Long, isFavorite: Boolean) -> Unit,
    onUpdateSnippet: (buttonId: Long, name: String, value: String, isFavorite: Boolean) -> Unit,
    onDeleteSnippet: (buttonId: Long) -> Unit,
    onClearToast: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearToast()
        }
    }

    var snippetToEdit by remember { mutableStateOf<ButtonEntity?>(null) }
    var snippetToDelete by remember { mutableStateOf<ButtonEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Cedar.colors.canvas,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search favorite snippets…", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(imageVector = Lucide.Search, contentDescription = null, tint = Cedar.colors.ink3, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Lucide.X, contentDescription = "Clear", tint = Cedar.colors.ink3, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Cedar.colors.surface,
                        unfocusedContainerColor = Cedar.colors.surface,
                        focusedBorderColor = Cedar.colors.spruce,
                        unfocusedBorderColor = Cedar.colors.lineStrong,
                        focusedTextColor = Cedar.colors.ink,
                        unfocusedTextColor = Cedar.colors.ink
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Lucide.Star,
                            contentDescription = null,
                            tint = Cedar.colors.ochre.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "No matching favorites" else "No starred snippets yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Cedar.colors.ink2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Star any snippet from the Snippets tab for instant access here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cedar.colors.ink4
                        )
                    }
                }
            } else {
                val columns = if (isExpanded) GridCells.Fixed(2) else GridCells.Fixed(1)

                LazyVerticalGrid(
                    columns = columns,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.favorites, key = { it.id }) { btn ->
                        SnippetCard(
                            button = btn,
                            onCopy = onCopy,
                            onToggleFavorite = { fav -> onToggleFavorite(btn.id, fav) },
                            onEdit = { snippetToEdit = btn },
                            onDelete = { snippetToDelete = btn }
                        )
                    }
                }
            }
        }
    }

    // Edit Snippet Dialog
    snippetToEdit?.let { btn ->
        AddEditSnippetDialog(
            initialName = btn.buttonName,
            initialValue = btn.pasteValue,
            initialSectionId = btn.sectionId,
            initialIsFavorite = btn.isFavorite,
            availableSections = state.allSections,
            onDismiss = { snippetToEdit = null },
            onSave = { name, value, secId, isFav ->
                snippetToEdit = null
                onUpdateSnippet(btn.id, name, value, isFav)
            }
        )
    }

    // Delete Snippet Dialog
    snippetToDelete?.let { btn ->
        DeleteConfirmationDialog(
            title = "Delete Snippet",
            message = "Are you sure you want to delete \"${btn.buttonName}\"?",
            onConfirm = {
                onDeleteSnippet(btn.id)
                snippetToDelete = null
            },
            onDismiss = { snippetToDelete = null }
        )
    }
}
