package online.codyuhi.copypastetool.ui.sections

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import online.codyuhi.copypastetool.data.db.SectionEntity
import online.codyuhi.copypastetool.ui.components.AddEditSectionDialog
import online.codyuhi.copypastetool.ui.components.CedarBadge
import online.codyuhi.copypastetool.ui.components.CedarButton
import online.codyuhi.copypastetool.ui.components.CedarCard
import online.codyuhi.copypastetool.ui.components.DeleteConfirmationDialog
import online.codyuhi.copypastetool.ui.components.Lucide
import online.codyuhi.copypastetool.ui.theme.Cedar

@Composable
fun SectionsScreen(
    state: SectionsUiState,
    onAddSection: (name: String) -> Unit,
    onRenameSection: (sectionId: Long, newName: String) -> Unit,
    onDeleteSection: (sectionId: Long) -> Unit,
    onClearToast: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onClearToast()
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var sectionToEdit by remember { mutableStateOf<SectionEntity?>(null) }
    var sectionToDelete by remember { mutableStateOf<SectionEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Cedar.colors.spruce,
                contentColor = Cedar.colors.onSpruce,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = "New Section",
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Categories & Sections",
                    style = MaterialTheme.typography.titleLarge,
                    color = Cedar.colors.ink,
                    fontWeight = FontWeight.Bold
                )
                CedarBadge(text = "${state.sections.size} total")
            }

            if (state.sections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Lucide.Folder,
                            contentDescription = null,
                            tint = Cedar.colors.ink4,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No sections configured",
                            style = MaterialTheme.typography.titleMedium,
                            color = Cedar.colors.ink2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Group your copy-paste buttons into organized categories.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cedar.colors.ink4
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CedarButton(
                            text = "Create Section",
                            onClick = { showAddDialog = true }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.sections, key = { it.section.id }) { item ->
                        CedarCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Lucide.Folder,
                                        contentDescription = null,
                                        tint = Cedar.colors.spruce,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.section.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Cedar.colors.ink,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${item.buttons.size} snippets",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Cedar.colors.ink3
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { sectionToEdit = item.section },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Lucide.Edit3,
                                            contentDescription = "Rename",
                                            tint = Cedar.colors.ink2,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { sectionToDelete = item.section },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Lucide.Trash2,
                                            contentDescription = "Delete",
                                            tint = Cedar.colors.rust,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditSectionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name ->
                showAddDialog = false
                onAddSection(name)
            }
        )
    }

    sectionToEdit?.let { sec ->
        AddEditSectionDialog(
            initialName = sec.name,
            onDismiss = { sectionToEdit = null },
            onSave = { name ->
                sectionToEdit = null
                onRenameSection(sec.id, name)
            }
        )
    }

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
