package online.codyuhi.copypastetool.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import online.codyuhi.copypastetool.ui.components.Lucide

@Serializable
sealed interface NavDestination {
    val title: String
    val icon: ImageVector

    @Serializable
    data object Snippets : NavDestination {
        override val title: String = "Snippets"
        override val icon: ImageVector get() = Lucide.Copy
    }

    @Serializable
    data object Favorites : NavDestination {
        override val title: String = "Favorites"
        override val icon: ImageVector get() = Lucide.Star
    }

    @Serializable
    data object Sections : NavDestination {
        override val title: String = "Sections"
        override val icon: ImageVector get() = Lucide.Folder
    }

    @Serializable
    data object Settings : NavDestination {
        override val title: String = "Settings"
        override val icon: ImageVector get() = Lucide.Settings
    }
}
