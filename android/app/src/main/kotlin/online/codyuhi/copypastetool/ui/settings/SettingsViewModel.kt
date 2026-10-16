package online.codyuhi.copypastetool.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import online.codyuhi.copypastetool.data.repository.CopyPasteRepository
import online.codyuhi.copypastetool.data.repository.SettingsRepository
import online.codyuhi.copypastetool.data.repository.SyncStatus
import online.codyuhi.copypastetool.ui.theme.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.System,
    val username: String? = null,
    val authToken: String? = null,
    val lastSyncTime: Long? = null,
    val hasLocalNetworkAccess: Boolean = false,
    val isWaking: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val copyPasteRepository: CopyPasteRepository,
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _successMessage = MutableStateFlow<String?>(null)

    private data class SettingsData(
        val theme: ThemeMode,
        val username: String?,
        val authToken: String?,
        val lastSync: Long?,
        val localNet: Boolean,
    )

    private val settingsData = combine(
        settingsRepository.themeMode,
        settingsRepository.username,
        settingsRepository.authToken,
        settingsRepository.lastSyncTime,
        settingsRepository.hasLocalNetworkAccess
    ) { theme, user, token, lastSync, localNet ->
        SettingsData(theme, user, token, lastSync, localNet)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsData,
        copyPasteRepository.isWaking,
        copyPasteRepository.syncStatus,
        _errorMessage,
        _successMessage
    ) { settings, waking, sync, err, success ->
        SettingsUiState(
            themeMode = settings.theme,
            username = settings.username,
            authToken = settings.authToken,
            lastSyncTime = settings.lastSync,
            hasLocalNetworkAccess = settings.localNet,
            isWaking = waking,
            syncStatus = sync,
            errorMessage = err,
            successMessage = success
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun login(user: String, pass: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            _successMessage.value = null
            val result = copyPasteRepository.login(user, pass)
            if (result.isSuccess) {
                _successMessage.value = "Logged in as $user"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed"
            }
        }
    }

    fun register(user: String, pass: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            _successMessage.value = null
            val result = copyPasteRepository.register(user, pass)
            if (result.isSuccess) {
                _successMessage.value = "Account created & snippets synced"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            copyPasteRepository.logout()
            _successMessage.value = "Logged out"
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _errorMessage.value = null
            _successMessage.value = null
            val result = copyPasteRepository.syncWithServer()
            if (result.isSuccess) {
                _successMessage.value = "Synced with homelab backend"
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Sync failed"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
