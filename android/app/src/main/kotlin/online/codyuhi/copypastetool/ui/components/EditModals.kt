package online.codyuhi.copypastetool.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import online.codyuhi.copypastetool.data.db.SectionEntity
import online.codyuhi.copypastetool.ui.theme.Cedar
import online.codyuhi.copypastetool.ui.theme.Mono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSnippetDialog(
    initialName: String = "",
    initialValue: String = "",
    initialSectionId: Long = 0,
    initialIsFavorite: Boolean = false,
    availableSections: List<SectionEntity>,
    onDismiss: () -> Unit,
    onSave: (name: String, value: String, sectionId: Long, isFavorite: Boolean) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var value by remember { mutableStateOf(initialValue) }
    var selectedSectionId by remember {
        mutableLongStateOf(if (initialSectionId > 0) initialSectionId else (availableSections.firstOrNull()?.id ?: 0L))
    }
    var isFavorite by remember { mutableStateOf(initialIsFavorite) }
    var sectionDropdownExpanded by remember { mutableStateOf(false) }

    val isEditing = initialName.isNotEmpty() || initialValue.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Edit Snippet" else "Add New Snippet",
                style = MaterialTheme.typography.titleLarge,
                color = Cedar.colors.ink
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section selector
                if (availableSections.isNotEmpty()) {
                    Text(
                        text = "Section",
                        style = MaterialTheme.typography.labelLarge,
                        color = Cedar.colors.ink2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = sectionDropdownExpanded,
                        onExpandedChange = { sectionDropdownExpanded = it }
                    ) {
                        val currentSectionName = availableSections.find { it.id == selectedSectionId }?.name ?: "Select section"
                        OutlinedTextField(
                            value = currentSectionName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cedar.colors.spruce,
                                unfocusedBorderColor = Cedar.colors.lineStrong,
                                focusedTextColor = Cedar.colors.ink,
                                unfocusedTextColor = Cedar.colors.ink
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = sectionDropdownExpanded,
                            onDismissRequest = { sectionDropdownExpanded = false }
                        ) {
                            availableSections.forEach { section ->
                                DropdownMenuItem(
                                    text = { Text(section.name) },
                                    onClick = {
                                        selectedSectionId = section.id
                                        sectionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Button Name
                Text(
                    text = "Button Label",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cedar.colors.ink2
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Support Greeting, Email Intro") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cedar.colors.spruce,
                        unfocusedBorderColor = Cedar.colors.lineStrong,
                        focusedTextColor = Cedar.colors.ink,
                        unfocusedTextColor = Cedar.colors.ink
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Paste Value
                Text(
                    text = "Paste Value",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cedar.colors.ink2
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    placeholder = { Text("Text to copy to clipboard") },
                    minLines = 3,
                    maxLines = 6,
                    textStyle = Mono,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cedar.colors.spruce,
                        unfocusedBorderColor = Cedar.colors.lineStrong,
                        focusedTextColor = Cedar.colors.ink,
                        unfocusedTextColor = Cedar.colors.ink
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Star as favorite checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Cedar.colors.spruce,
                            uncheckedColor = Cedar.colors.lineStrong
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add to Favorites",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cedar.colors.ink2
                    )
                }
            }
        },
        confirmButton = {
            CedarButton(
                text = "Save",
                enabled = name.isNotBlank() && value.isNotBlank() && selectedSectionId > 0,
                onClick = {
                    onSave(name.trim(), value, selectedSectionId, isFavorite)
                }
            )
        },
        dismissButton = {
            CedarOutlinedButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        containerColor = Cedar.colors.surface,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun AddEditSectionDialog(
    initialName: String = "",
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialName.isNotEmpty()) "Edit Section" else "New Section",
                style = MaterialTheme.typography.titleLarge,
                color = Cedar.colors.ink
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Section Name",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cedar.colors.ink2
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Billing, Technical, Links") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cedar.colors.spruce,
                        unfocusedBorderColor = Cedar.colors.lineStrong,
                        focusedTextColor = Cedar.colors.ink,
                        unfocusedTextColor = Cedar.colors.ink
                    )
                )
            }
        },
        confirmButton = {
            CedarButton(
                text = "Save",
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim()) }
            )
        },
        dismissButton = {
            CedarOutlinedButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        containerColor = Cedar.colors.surface,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Cedar.colors.rust
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Cedar.colors.ink2
            )
        },
        confirmButton = {
            CedarDangerButton(
                text = "Delete",
                onClick = onConfirm
            )
        },
        dismissButton = {
            CedarOutlinedButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        containerColor = Cedar.colors.surface,
        shape = MaterialTheme.shapes.medium
    )
}
