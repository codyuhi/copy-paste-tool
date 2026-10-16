package online.codyuhi.copypastetool.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import online.codyuhi.copypastetool.data.db.ButtonEntity
import online.codyuhi.copypastetool.data.db.SectionEntity
import online.codyuhi.copypastetool.data.repository.CopyPasteRepository

data class FavoritesUiState(
    val favorites: List<ButtonEntity> = emptyList(),
    val allSections: List<SectionEntity> = emptyList(),
    val searchQuery: String = "",
    val toastMessage: String? = null,
)

class FavoritesViewModel(
    private val repository: CopyPasteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _toastMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FavoritesUiState> = combine(
        repository.favorites,
        repository.sectionsWithButtons,
        _searchQuery,
        _toastMessage
    ) { favorites, sections, query, toast ->
        val filtered = if (query.isBlank()) {
            favorites
        } else {
            favorites.filter {
                it.buttonName.contains(query, ignoreCase = true) ||
                    it.pasteValue.contains(query, ignoreCase = true)
            }
        }

        FavoritesUiState(
            favorites = filtered,
            allSections = sections.map { it.section },
            searchQuery = query,
            toastMessage = toast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FavoritesUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSnippetCopied(name: String) {
        _toastMessage.value = "Copied \"$name\" to clipboard"
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun toggleFavorite(buttonId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(buttonId, isFavorite)
        }
    }

    fun updateSnippet(buttonId: Long, name: String, value: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateButton(buttonId, name, value, isFavorite)
            _toastMessage.value = "Updated \"$name\""
        }
    }

    fun deleteSnippet(buttonId: Long) {
        viewModelScope.launch {
            repository.deleteButton(buttonId)
            _toastMessage.value = "Deleted snippet"
        }
    }
}
