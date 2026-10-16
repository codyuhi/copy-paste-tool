package online.codyuhi.copypastetool.ui.snippets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import online.codyuhi.copypastetool.data.db.SectionEntity
import online.codyuhi.copypastetool.data.repository.CopyPasteRepository
import online.codyuhi.copypastetool.data.repository.SectionWithButtons
import online.codyuhi.copypastetool.data.repository.SyncStatus

data class SnippetsUiState(
    val sections: List<SectionWithButtons> = emptyList(),
    val allSections: List<SectionEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedSectionFilter: Long? = null,
    val isWaking: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val toastMessage: String? = null,
)

class SnippetsViewModel(
    private val repository: CopyPasteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedSectionFilter = MutableStateFlow<Long?>(null)
    private val _toastMessage = MutableStateFlow<String?>(null)

    private val filteredSections = combine(
        repository.sectionsWithButtons,
        _searchQuery,
        _selectedSectionFilter
    ) { sections, query, filter ->
        val filtered = sections.mapNotNull { secWithBtns ->
            if (filter != null && secWithBtns.section.id != filter) {
                return@mapNotNull null
            }

            val matchingButtons = if (query.isBlank()) {
                secWithBtns.buttons
            } else {
                secWithBtns.buttons.filter { btn ->
                    btn.buttonName.contains(query, ignoreCase = true) ||
                        btn.pasteValue.contains(query, ignoreCase = true)
                }
            }

            if (matchingButtons.isNotEmpty() || (query.isBlank() && filter == secWithBtns.section.id)) {
                secWithBtns.copy(buttons = matchingButtons)
            } else {
                null
            }
        }
        Triple(filtered, sections.map { it.section }, query to filter)
    }

    val uiState: StateFlow<SnippetsUiState> = combine(
        filteredSections,
        repository.isWaking,
        repository.syncStatus,
        _toastMessage
    ) { (filtered, allSectionEntities, qf), waking, sync, toast ->
        val (query, filter) = qf
        SnippetsUiState(
            sections = filtered,
            allSections = allSectionEntities,
            searchQuery = query,
            selectedSectionFilter = filter,
            isWaking = waking,
            syncStatus = sync,
            toastMessage = toast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SnippetsUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSectionFilterChange(sectionId: Long?) {
        _selectedSectionFilter.value = if (_selectedSectionFilter.value == sectionId) null else sectionId
    }

    fun onSnippetCopied(name: String) {
        _toastMessage.value = "Copied \"$name\" to clipboard"
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun addSnippet(name: String, value: String, sectionId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            val id = repository.addButton(sectionId, name, value)
            if (isFavorite) {
                repository.toggleFavorite(id, true)
            }
            _toastMessage.value = "Added \"$name\""
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

    fun toggleFavorite(buttonId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(buttonId, isFavorite)
        }
    }

    fun addSection(name: String) {
        viewModelScope.launch {
            repository.addSection(name)
            _toastMessage.value = "Added section \"$name\""
        }
    }

    fun deleteSection(sectionId: Long) {
        viewModelScope.launch {
            repository.deleteSection(sectionId)
            _toastMessage.value = "Deleted section"
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.syncWithServer()
        }
    }
}
