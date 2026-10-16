package online.codyuhi.copypastetool.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CopyButton(
    val buttonName: String,
    val pasteValue: String,
)

@Serializable
data class CopySection(
    val sectionName: String,
    val sectionButtons: List<CopyButton> = emptyList(),
)

@Serializable
data class FavoriteButton(
    val buttonName: String,
    val pasteValue: String,
    val sectionId: Int = 0,
    val buttonId: Int = 0,
)

@Serializable
data class UserDataPayload(
    val sections: List<CopySection> = emptyList(),
    val favorites: List<FavoriteButton> = emptyList(),
    val updatedAt: String? = null,
)

@Serializable
data class User(
    val id: String,
    val username: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val initialSections: List<CopySection> = emptyList(),
    val initialFavorites: List<FavoriteButton> = emptyList(),
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User,
)

@Serializable
data class GenericResponse(
    val success: Boolean = true,
    val message: String? = null,
    val error: String? = null,
    val updatedAt: String? = null,
)
