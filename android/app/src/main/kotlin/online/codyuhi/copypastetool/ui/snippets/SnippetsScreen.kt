package online.codyuhi.copypastetool.ui.snippets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowWidthSizeClass
import online.codyuhi.copypastetool.data.db.ButtonEntity
import online.codyuhi.copypastetool.data.db.SectionEntity
import online.codyuhi.copypastetool.ui.components.AddEditSectionDialog
import online.codyuhi.copypastetool.ui.components.AddEditSnippetDialog
import online.codyuhi.copypastetool.ui.components.CedarBadge
import online.codyuhi.copypastetool.ui.components.CedarButton
import online.codyuhi.copypastetool.ui.components.CedarCard
import online.codyuhi.copypastetool.ui.components.CedarOutlinedButton
import online.codyuhi.copypastetool.ui.components.DeleteConfirmationDialog
import online.codyuhi.copypastetool.ui.components.Lucide
import online.codyuhi.copypastetool.ui.components.SablierWakingBanner
import online.codyuhi.copypastetool.ui.components.SnippetCard
import online.codyuhi.copypastetool.ui.theme.Cedar
import online.codyuhi.copypastetool.ui.theme.MonoSmall

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetsScreen(
    state: SnippetsUiState,
    onSearchChange: (String) -> Unit,
    onSectionFilterChange: (Long?) -> Unit,
    onCopy: (String) -> Unit,
    onToggleFavorite: (buttonId: Long, isFavorite: Boolean) -> Unit,
    onAddSnippet: (name: String, value: String, sectionId: Long, isFavorite: Boolean) -> Unit,
    onUpdateSnippet: (buttonId: Long, name: String, value: String, isFavorite: Boolean) -> Unit,
    onDeleteSnippet: (buttonId: Long) -> Unit,
    onAddSection: (name: String) -> Unit,
    onDeleteSection: (sectionId: Long) -> Unit,
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

    var showAddSnippetDialog by remember { mutableStateOf(false) }
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var snippetToEdit by remember { mutableStateOf<ButtonEntity?>(null) }
    var snippetToDelete by remember { mutableStateOf<ButtonEntity?>(null) }
    var sectionToDelete by remember { mutableStateOf<SectionEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (state.allSections.isEmpty()) {
                        showAddSectionDialog = true
                    } else {
                        showAddSnippetDialog = true
                    }
                },
                containerColor = Cedar.colors.spruce,
                contentColor = Cedar.colors.onSpruce,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = "Add Snippet",
                    tint = Cedar.colors.onSpruce,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = Cedar.colors.canvas,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sablier Waking Banner
            AnimatedVisibility(visible = state.isWaking) {
                SablierWakingBanner()
            }

            // Search Bar & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search snippets or paste value…", style = MaterialTheme.typography.bodyMedium) },
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
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // New Section Button
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Cedar.colors.surface,
                    border = BorderStroke(1.dp, Cedar.colors.lineStrong),
                    modifier = Modifier
                        .height(48.dp)
                        .clip(MaterialTheme.shapes.small)
                        .clickable { showAddSectionDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Lucide.Folder, contentDescription = null, tint = Cedar.colors.spruce, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Lucide.Plus, contentDescription = null, tint = Cedar.colors.spruce, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Section Filter Chips
            if (state.allSections.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            label = "All",
                            isSelected = state.selectedSectionFilter == null,
                            onClick = { onSectionFilterChange(null) }
                        )
                    }
                    items(state.allSections, key = { it.id }) { sec ->
                        FilterChip(
                            label = sec.name,
                            isSelected = state.selectedSectionFilter == sec.id,
                            onClick = { onSectionFilterChange(sec.id) }
                        )
                    }
                }
            }

            // Empty state
            if (state.sections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Lucide.Copy,
                            contentDescription = null,
                            tint = Cedar.colors.ink4,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "No matching snippets found" else "No snippets yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Cedar.colors.ink2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "Try a different search term" else "Create a section and add your first copy-paste button.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cedar.colors.ink4
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CedarButton(
                            text = if (state.allSections.isEmpty()) "Create First Section" else "Add Snippet",
                            onClick = {
                                if (state.allSections.isEmpty()) showAddSectionDialog = true else showAddSnippetDialog = true
                            }
                        )
                    }
                }
            } else {
                // Adaptive Snippets Layout: Grid on Fold unfolded (Medium/Expanded), List on Compact
                val columns = if (isExpanded) GridCells.Fixed(2) else GridCells.Fixed(1)

                LazyVerticalGrid(
                    columns = columns,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.sections.forEach { secWithBtns ->
                        // Section Header spanning all columns
                        item(span = { GridItemSpan(maxLineSpan) }, key = "header_${secWithBtns.section.id}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = secWithBtns.section.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Cedar.colors.ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    CedarBadge(text = "${secWithBtns.buttons.size}")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            showAddSnippetDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Lucide.Plus,
                                            contentDescription = "Add snippet to section",
                                            tint = Cedar.colors.spruce,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { sectionToDelete = secWithBtns.section },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Lucide.Trash2,
                                            contentDescription = "Delete Section",
                                            tint = Cedar.colors.ink4,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Snippet items
                        items(secWithBtns.buttons, key = { it.id }) { btn ->
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
    }

    // Add Snippet Dialog
    if (showAddSnippetDialog) {
        AddEditSnippetDialog(
            availableSections = state.allSections,
            onDismiss = { showAddSnippetDialog = false },
            onSave = { name, value, secId, isFav ->
                showAddSnippetDialog = false
                onAddSnippet(name, value, secId, isFav)
            }
        )
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
            message = "Are you sure you want to delete \"${btn.buttonName}\"? This cannot be undone.",
            onConfirm = {
                onDeleteSnippet(btn.id)
                snippetToDelete = null
            },
            onDismiss = { snippetToDelete = null }
        )
    }

    // Add Section Dialog
    if (showAddSectionDialog) {
        AddEditSectionDialog(
            onDismiss = { showAddSectionDialog = false },
            onSave = { name ->
                showAddSectionDialog = false
                onAddSection(name)
            }
        )
    }

    // Delete Section Dialog
    sectionToDelete?.let { sec ->
        DeleteConfirmationDialog(
            title = "Delete Section",
            message = "Are you sure you want to delete section \"${sec.name}\" and all of its snippets?",
            onConfirm = {
                onDeleteSection(sec.id)
                sectionToDelete = null
            },
            onDismiss = { sectionToDelete = null }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) Cedar.colors.spruce else Cedar.colors.surface
    val border = if (isSelected) Cedar.colors.spruce else Cedar.colors.lineStrong
    val textColor = if (isSelected) Cedar.colors.onSpruce else Cedar.colors.ink2

    Surface(
        shape = CircleShape,
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
