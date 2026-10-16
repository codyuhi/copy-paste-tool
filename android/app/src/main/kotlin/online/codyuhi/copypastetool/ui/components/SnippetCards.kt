package online.codyuhi.copypastetool.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import online.codyuhi.copypastetool.data.db.ButtonEntity
import online.codyuhi.copypastetool.ui.theme.Cedar
import online.codyuhi.copypastetool.ui.theme.MonoSmall

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SnippetCard(
    button: ButtonEntity,
    onCopy: (String) -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var isCopied by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(1500)
            isCopied = false
        }
    }

    val cardBorderColor by animateColorAsState(
        targetValue = if (isCopied) Cedar.colors.spruce else Cedar.colors.line,
        animationSpec = tween(200),
        label = "cardBorder"
    )

    val cardBgColor by animateColorAsState(
        targetValue = if (isCopied) Cedar.colors.okBg else Cedar.colors.surface,
        animationSpec = tween(200),
        label = "cardBg"
    )

    Surface(
        shape = MaterialTheme.shapes.small,
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor),
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(button.buttonName, button.pasteValue)
                    clipboard.setPrimaryClip(clip)
                    isCopied = true
                    onCopy(button.buttonName)
                },
                onLongClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    menuExpanded = true
                }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = button.buttonName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Cedar.colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (button.isFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Lucide.Star,
                            contentDescription = "Favorite",
                            tint = Cedar.colors.ochre,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = button.pasteValue,
                    style = MonoSmall,
                    color = Cedar.colors.ink3,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Copy indicator icon
            Surface(
                shape = CircleShape,
                color = if (isCopied) Cedar.colors.spruce else Cedar.colors.inset,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCopied) Lucide.Check else Lucide.Copy,
                        contentDescription = "Copy",
                        tint = if (isCopied) Cedar.colors.onSpruce else Cedar.colors.ink2,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Context Menu dropdown
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (button.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Lucide.Star,
                            contentDescription = null,
                            tint = if (button.isFavorite) Cedar.colors.ochre else Cedar.colors.ink3
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onToggleFavorite(!button.isFavorite)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Edit Snippet", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(imageVector = Lucide.Edit3, contentDescription = null, tint = Cedar.colors.ink2)
                    },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Snippet", style = MaterialTheme.typography.bodyMedium, color = Cedar.colors.rust) },
                    leadingIcon = {
                        Icon(imageVector = Lucide.Trash2, contentDescription = null, tint = Cedar.colors.rust)
                    },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}
