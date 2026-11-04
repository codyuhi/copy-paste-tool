package online.codyuhi.copypastetool.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import online.codyuhi.copypastetool.domain.TemplateHelper
import online.codyuhi.copypastetool.ui.theme.Cedar
import online.codyuhi.copypastetool.ui.theme.MonoSmall

@Composable
fun TemplatePromptDialog(
    snippetName: String,
    templateText: String,
    placeholders: List<String>,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit,
) {
    val variableMap = remember {
        mutableStateMapOf<String, String>().apply {
            placeholders.forEach { put(it, "") }
        }
    }

    val previewText = remember(variableMap.toMap()) {
        TemplateHelper.interpolate(templateText, variableMap)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Fill Template",
                    style = MaterialTheme.typography.titleLarge,
                    color = Cedar.colors.ink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = snippetName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Cedar.colors.ink3
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter values for template parameters:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Cedar.colors.ink2
                )

                placeholders.forEach { placeholder ->
                    OutlinedTextField(
                        value = variableMap[placeholder] ?: "",
                        onValueChange = { variableMap[placeholder] = it },
                        label = {
                            Text("${TemplateHelper.formatLabel(placeholder)} ({{$placeholder}})")
                        },
                        placeholder = { Text("Enter $placeholder…") },
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "RESULT PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cedar.colors.ink3,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Cedar.colors.inset,
                    border = BorderStroke(1.dp, Cedar.colors.line),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = previewText,
                        style = MonoSmall,
                        color = Cedar.colors.ink,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            CedarButton(
                text = "Copy to Clipboard",
                onClick = {
                    onCopy(previewText)
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Cedar.colors.ink3)
            }
        },
        containerColor = Cedar.colors.surface,
        shape = MaterialTheme.shapes.medium
    )
}
