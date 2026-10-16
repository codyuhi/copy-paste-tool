package online.codyuhi.copypastetool.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.codyuhi.copypastetool.ui.theme.Cedar
import online.codyuhi.copypastetool.ui.theme.MonoSmall
import online.codyuhi.copypastetool.ui.theme.ThemeMode

@Composable
fun CedarThemeToggle(
    currentMode: ThemeMode,
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (isDark) "Switch to light theme" else "Switch to dark theme"
    Surface(
        shape = CircleShape,
        color = Cedar.colors.surface,
        border = BorderStroke(1.dp, Cedar.colors.lineStrong),
        modifier = modifier.size(44.dp)
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = if (isDark) Lucide.Sun else Lucide.Moon,
                contentDescription = label,
                tint = if (isDark) Cedar.colors.ochre else Cedar.colors.spruce,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CedarButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Cedar.colors.spruce,
            contentColor = Cedar.colors.onSpruce,
            disabledContainerColor = Cedar.colors.line,
            disabledContentColor = Cedar.colors.ink4
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier.height(48.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = Cedar.colors.onSpruce)
    }
}

@Composable
fun CedarOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, Cedar.colors.lineStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Cedar.colors.surface,
            contentColor = Cedar.colors.ink2
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier.height(48.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = Cedar.colors.ink2)
    }
}

@Composable
fun CedarDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Cedar.colors.rust,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = modifier.height(48.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
fun CedarCard(
    modifier: Modifier = Modifier,
    border: BorderStroke = BorderStroke(1.dp, Cedar.colors.line),
    containerColor: Color = Cedar.colors.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = cardModifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
fun CedarBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Cedar.colors.spruce,
    bgColor: Color = Cedar.colors.inset,
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = color,
            style = MonoSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    message: String = "Offline: showing saved data from local storage",
) {
    Surface(
        color = Cedar.colors.warnBg,
        border = BorderStroke(1.dp, Cedar.colors.warn.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.CloudOff,
                contentDescription = null,
                tint = Cedar.colors.warn,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Cedar.colors.warn
            )
        }
    }
}

@Composable
fun SablierWakingBanner(
    modifier: Modifier = Modifier,
    message: String = "Waking up copy-paste-tool on MiniPC cluster…",
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Surface(
        color = Cedar.colors.okBg,
        border = BorderStroke(1.dp, Cedar.colors.ok.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Lucide.RefreshCw,
                contentDescription = null,
                tint = Cedar.colors.ok,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(angle)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Cedar.colors.ok
            )
        }
    }
}

@Composable
fun LocalNetworkBanner(
    onRequestAccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Cedar.colors.warnBg,
        border = BorderStroke(1.dp, Cedar.colors.warn.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Local Network Access",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cedar.colors.warn
                )
                Text(
                    text = "Allow local network access so Copy-Paste Tool can reach your homelab on home Wi-Fi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Cedar.colors.warn
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            CedarButton(
                text = "Allow",
                onClick = onRequestAccess,
                modifier = Modifier.height(36.dp)
            )
        }
    }
}
