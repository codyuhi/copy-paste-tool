package online.codyuhi.copypastetool.ui.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import online.codyuhi.copypastetool.data.repository.CopyPasteRepository
import online.codyuhi.copypastetool.data.repository.SectionWithButtons

data class SectionsUiState(
    val sections: List<SectionWithButtons> = emptyList(),
    val toastMessage: String? = null,
)

class SectionsViewModel(
    private val repository: CopyPasteRepository
) : ViewModel() {

    private val _toastMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SectionsUiState> = combine(
        repository.sectionsWithButtons,
        _toastMessage
    ) { sections, toast ->
        SectionsUiState(sections = sections, toastMessage = toast)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SectionsUiState()
    )

    fun clearToast() {
        _toastMessage.value = null
    }

    fun addSection(name: String) {
        viewModelScope.launch {
            repository.addSection(name)
            _toastMessage.value = "Added section \"$name\""
        }
    }

    fun renameSection(sectionId: Long, newName: String) {
        viewModelScope.launch {
            repository.updateSection(sectionId, newName)
            _toastMessage.value = "Renamed section to \"$newName\""
        }
    }

    fun deleteSection(sectionId: Long) {
        viewModelScope.launch {
            repository.deleteSection(sectionId)
            _toastMessage.value = "Deleted section"
        }
    }
}
