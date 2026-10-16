package online.codyuhi.copypastetool.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private fun lucide(name: String, vararg paths: String): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEach {
            addPath(
                pathData = addPathNodes(it),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object Lucide {
    val Copy: ImageVector by lazy {
        lucide(
            "copy",
            "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2",
            "M8 8h12c1.1 0 2 .9 2 2v10c0 1.1-.9 2-2 2H8c-1.1 0-2-.9-2-2V10c0-1.1.9-2 2-2z",
        )
    }

    val Check: ImageVector by lazy {
        lucide(
            "check",
            "M20 6 9 17l-5-5",
        )
    }

    val Search: ImageVector by lazy {
        lucide(
            "search",
            "M2 11a9 9 0 1 0 18 0a9 9 0 1 0 -18 0",
            "m21 21-4.35-4.35",
        )
    }

    val Plus: ImageVector by lazy {
        lucide(
            "plus",
            "M5 12h14",
            "M12 5v14",
        )
    }

    val Star: ImageVector by lazy {
        lucide(
            "star",
            "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z",
        )
    }

    val Trash2: ImageVector by lazy {
        lucide(
            "trash-2",
            "M3 6h18",
            "M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6",
            "M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2",
            "M10 11v6",
            "M14 11v6",
        )
    }

    val Edit3: ImageVector by lazy {
        lucide(
            "edit-3",
            "M12 20h9",
            "M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z",
        )
    }

    val Settings: ImageVector by lazy {
        lucide(
            "settings",
            "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z",
            "M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z",
        )
    }

    val Moon: ImageVector by lazy {
        lucide(
            "moon",
            "M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z",
        )
    }

    val Sun: ImageVector by lazy {
        lucide(
            "sun",
            "M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8z",
            "M12 2v2",
            "M12 20v2",
            "m4.93 4.93 1.41 1.41",
            "m17.66 17.66 1.41 1.41",
            "M2 12h2",
            "M20 12h2",
            "m6.34 17.66-1.41 1.41",
            "m19.07 4.93-1.41 1.41",
        )
    }

    val Cloud: ImageVector by lazy {
        lucide(
            "cloud",
            "M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z",
        )
    }

    val CloudOff: ImageVector by lazy {
        lucide(
            "cloud-off",
            "m2 2 20 20",
            "M5.782 5.782A7 7 0 0 0 9 19h8.5a4.5 4.5 0 0 0 1.307-.193",
            "M21.532 16.5A4.5 4.5 0 0 0 17.5 10h-1.79A7.008 7.008 0 0 0 10 5.07",
        )
    }

    val RefreshCw: ImageVector by lazy {
        lucide(
            "refresh-cw",
            "M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8",
            "M21 3v5h-5",
            "M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16",
            "M8 16H3v5",
        )
    }

    val Folder: ImageVector by lazy {
        lucide(
            "folder",
            "M4 20h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.93a2 2 0 0 1-1.66-.9l-.82-1.2A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13c0 1.1.9 2 2 2Z",
        )
    }

    val User: ImageVector by lazy {
        lucide(
            "user",
            "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2",
            "M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z",
        )
    }

    val LogIn: ImageVector by lazy {
        lucide(
            "log-in",
            "M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4",
            "M10 17l5-5-5-5",
            "M15 12H3",
        )
    }

    val LogOut: ImageVector by lazy {
        lucide(
            "log-out",
            "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
            "M16 17l5-5-5-5",
            "M21 12H9",
        )
    }

    val Share2: ImageVector by lazy {
        lucide(
            "share-2",
            "M18 5a3 3 0 1 0 0 6 3 3 0 0 0 0-6z",
            "M6 12a3 3 0 1 0 0 6 3 3 0 0 0 0-6z",
            "M18 19a3 3 0 1 0 0 6 3 3 0 0 0 0-6z",
            "m8.59 13.51 6.83 3.98",
            "m15.41 6.51-6.82 3.98",
        )
    }

    val MoreVertical: ImageVector by lazy {
        lucide(
            "more-vertical",
            "M12 11a1 1 0 1 0 0 2 1 1 0 0 0 0-2z",
            "M12 4a1 1 0 1 0 0 2 1 1 0 0 0 0-2z",
            "M12 18a1 1 0 1 0 0 2 1 1 0 0 0 0-2z",
        )
    }

    val X: ImageVector by lazy {
        lucide(
            "x",
            "M18 6 6 18",
            "m6 6 12 12",
        )
    }
}
