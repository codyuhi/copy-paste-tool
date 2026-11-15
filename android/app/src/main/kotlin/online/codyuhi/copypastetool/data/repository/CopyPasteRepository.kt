package online.codyuhi.copypastetool.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import online.codyuhi.copypastetool.data.api.CopyPasteApi
import online.codyuhi.copypastetool.data.api.SablierInterceptor
import online.codyuhi.copypastetool.data.db.ButtonEntity
import online.codyuhi.copypastetool.data.db.CopyPasteDao
import online.codyuhi.copypastetool.data.db.OutboxDao
import online.codyuhi.copypastetool.data.db.OutboxEntity
import online.codyuhi.copypastetool.data.db.SectionEntity
import online.codyuhi.copypastetool.data.model.CopyButton
import online.codyuhi.copypastetool.data.model.CopySection
import online.codyuhi.copypastetool.data.model.FavoriteButton
import online.codyuhi.copypastetool.data.model.LoginRequest
import online.codyuhi.copypastetool.data.model.RegisterRequest
import online.codyuhi.copypastetool.data.model.UserDataPayload

data class SectionWithButtons(
    val section: SectionEntity,
    val buttons: List<ButtonEntity>
)

sealed interface SyncStatus {
    object Idle : SyncStatus
    object Syncing : SyncStatus
    data class Success(val message: String = "Synced with homelab") : SyncStatus
    data class Error(val message: String) : SyncStatus
}

class CopyPasteRepository(
    private val copyPasteDao: CopyPasteDao,
    private val outboxDao: OutboxDao,
    private val api: CopyPasteApi,
    private val settingsRepository: SettingsRepository,
    private val sablierInterceptor: SablierInterceptor,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val lihimStore: LihimStore = LihimStore(),
) {
    val isWaking: StateFlow<Boolean> = sablierInterceptor.isWaking

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // While true, every read and write goes to the in-memory lihim store instead
    // of Room, so the screens show only lihim content.
    private val _isLihim = MutableStateFlow(false)
    val isLihim: StateFlow<Boolean> = _isLihim.asStateFlow()
    private val lihimPushLock = Mutex()

    private val roomSectionsWithButtons: Flow<List<SectionWithButtons>> =
        combine(copyPasteDao.observeSections(), copyPasteDao.observeButtons()) { sections, buttons ->
            val buttonsBySection = buttons.groupBy { it.sectionId }
            sections.map { section ->
                SectionWithButtons(
                    section = section,
                    buttons = buttonsBySection[section.id] ?: emptyList()
                )
            }
        }

    val sectionsWithButtons: Flow<List<SectionWithButtons>> =
        combine(_isLihim, roomSectionsWithButtons, lihimStore.sections) { lihim, room, hidden ->
            if (lihim) hidden else room
        }

    val favorites: Flow<List<ButtonEntity>> =
        combine(_isLihim, copyPasteDao.observeFavorites(), lihimStore.sections) { lihim, room, hidden ->
            if (lihim) lihimStore.favorites(hidden) else room
        }

    /**
     * Fetches and shows the lihim set. Returns false (and changes nothing) when the
     * server refuses, which it does with a 404 for anyone but a signed-in admin.
     */
    suspend fun openLihim(): Boolean {
        val token = settingsRepository.authToken.firstOrNull()
        if (token.isNullOrBlank()) return false
        return try {
            val res = api.getLihimData("Bearer $token")
            val body = res.body()
            if (res.isSuccessful && body != null) {
                lihimStore.load(body)
                _isLihim.value = true
                _syncStatus.value = SyncStatus.Idle
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun closeLihim() {
        if (!_isLihim.value) return
        _isLihim.value = false
        lihimStore.clear()
        _syncStatus.value = SyncStatus.Idle
    }

    private fun pushLihim() {
        scope.launch {
            lihimPushLock.withLock {
                if (!_isLihim.value) return@withLock
                val token = settingsRepository.authToken.firstOrNull()
                if (token.isNullOrBlank()) return@withLock
                _syncStatus.value = SyncStatus.Syncing
                _syncStatus.value = try {
                    val res = api.putLihimData("Bearer $token", lihimStore.toPayload())
                    if (res.isSuccessful) SyncStatus.Success("Saved") else SyncStatus.Error("Save failed: HTTP ${res.code()}")
                } catch (e: Exception) {
                    SyncStatus.Error(e.message ?: "Failed to connect to homelab")
                }
            }
        }
    }

    suspend fun addSection(name: String): Long {
        if (_isLihim.value) return lihimStore.addSection(name).also { pushLihim() }
        val count = copyPasteDao.getSections().size
        val id = copyPasteDao.insertSection(
            SectionEntity(
                name = name.trim(),
                sortOrder = count
            )
        )
        triggerSyncAfterMutation()
        return id
    }

    suspend fun updateSection(sectionId: Long, name: String) {
        if (_isLihim.value) {
            lihimStore.updateSection(sectionId, name)
            pushLihim()
            return
        }
        val existing = copyPasteDao.getSectionById(sectionId) ?: return
        copyPasteDao.updateSection(existing.copy(name = name.trim()))
        triggerSyncAfterMutation()
    }

    suspend fun deleteSection(sectionId: Long) {
        if (_isLihim.value) {
            lihimStore.deleteSection(sectionId)
            pushLihim()
            return
        }
        copyPasteDao.deleteSection(sectionId)
        triggerSyncAfterMutation()
    }

    suspend fun addButton(sectionId: Long, name: String, pasteValue: String): Long {
        if (_isLihim.value) return lihimStore.addButton(sectionId, name, pasteValue).also { pushLihim() }
        val count = copyPasteDao.getButtons().count { it.sectionId == sectionId }
        val id = copyPasteDao.insertButton(
            ButtonEntity(
                sectionId = sectionId,
                buttonName = name.trim(),
                pasteValue = pasteValue,
                sortOrder = count,
                isFavorite = false
            )
        )
        triggerSyncAfterMutation()
        return id
    }

    suspend fun updateButton(buttonId: Long, name: String, pasteValue: String, isFavorite: Boolean) {
        if (_isLihim.value) {
            lihimStore.updateButton(buttonId) { it.copy(buttonName = name.trim(), pasteValue = pasteValue, isFavorite = isFavorite) }
            pushLihim()
            return
        }
        val existing = copyPasteDao.getButtonById(buttonId) ?: return
        copyPasteDao.updateButton(
            existing.copy(
                buttonName = name.trim(),
                pasteValue = pasteValue,
                isFavorite = isFavorite
            )
        )
        triggerSyncAfterMutation()
    }

    suspend fun deleteButton(buttonId: Long) {
        if (_isLihim.value) {
            lihimStore.deleteButton(buttonId)
            pushLihim()
            return
        }
        copyPasteDao.deleteButton(buttonId)
        triggerSyncAfterMutation()
    }

    suspend fun toggleFavorite(buttonId: Long, isFavorite: Boolean) {
        if (_isLihim.value) {
            lihimStore.updateButton(buttonId) { it.copy(isFavorite = isFavorite) }
            pushLihim()
            return
        }
        copyPasteDao.setFavorite(buttonId, isFavorite)
        triggerSyncAfterMutation()
    }

    suspend fun seedDefaultsIfEmpty() {
        val existingSections = copyPasteDao.getSections()
        if (existingSections.isEmpty()) {
            val starterData = listOf(
                Pair(
                    SectionEntity(name = "Customer Support", sortOrder = 0),
                    listOf(
                        ButtonEntity(sectionId = 0, buttonName = "Greeting", pasteValue = "Hello! Thanks for reaching out. How can I help you today?", sortOrder = 0, isFavorite = true),
                        ButtonEntity(sectionId = 0, buttonName = "Closing", pasteValue = "Please let me know if you have any additional questions. Have a wonderful day!", sortOrder = 1, isFavorite = true),
                        ButtonEntity(sectionId = 0, buttonName = "Hold", pasteValue = "Thank you for your patience while I look into this for you. I will be right back with an update.", sortOrder = 2)
                    )
                ),
                Pair(
                    SectionEntity(name = "Common Links", sortOrder = 1),
                    listOf(
                        ButtonEntity(sectionId = 0, buttonName = "Home Portal", pasteValue = "https://home.lan.codyuhi.online", sortOrder = 0, isFavorite = true),
                        ButtonEntity(sectionId = 0, buttonName = "Tasks", pasteValue = "https://tasks.lan.codyuhi.online", sortOrder = 1),
                        ButtonEntity(sectionId = 0, buttonName = "Blog", pasteValue = "https://blog.lan.codyuhi.online", sortOrder = 2)
                    )
                ),
                Pair(
                    SectionEntity(name = "Code & Shell", sortOrder = 2),
                    listOf(
                        ButtonEntity(sectionId = 0, buttonName = "Git Status", pasteValue = "git status --short", sortOrder = 0),
                        ButtonEntity(sectionId = 0, buttonName = "K8s Pods", pasteValue = "kubectl get pods -A", sortOrder = 1)
                    )
                )
            )
            copyPasteDao.replaceAll(starterData)
        }
    }

    private fun triggerSyncAfterMutation() {
        scope.launch {
            val token = settingsRepository.authToken.firstOrNull()
            if (!token.isNullOrBlank()) {
                syncWithServer()
            }
        }
    }

    suspend fun syncWithServer(): Result<Unit> {
        // Room is untouched while lihim is open, so a sync then means "save lihim".
        if (_isLihim.value) {
            pushLihim()
            return Result.success(Unit)
        }
        val token = settingsRepository.authToken.firstOrNull()
        if (token.isNullOrBlank()) {
            _syncStatus.value = SyncStatus.Idle
            return Result.success(Unit)
        }

        _syncStatus.value = SyncStatus.Syncing
        val authHeader = "Bearer $token"

        return try {
            val currentSections = copyPasteDao.getSections()
            val currentButtons = copyPasteDao.getButtons()

            val buttonsBySection = currentButtons.groupBy { it.sectionId }

            val payloadSections = currentSections.map { sec ->
                CopySection(
                    sectionName = sec.name,
                    sectionButtons = (buttonsBySection[sec.id] ?: emptyList()).map { btn ->
                        CopyButton(
                            buttonName = btn.buttonName,
                            pasteValue = btn.pasteValue
                        )
                    }
                )
            }

            val payloadFavorites = currentButtons.filter { it.isFavorite }.mapIndexed { idx, btn ->
                FavoriteButton(
                    buttonName = btn.buttonName,
                    pasteValue = btn.pasteValue,
                    sectionId = 0,
                    buttonId = idx
                )
            }

            val payload = UserDataPayload(
                sections = payloadSections,
                favorites = payloadFavorites
            )

            val putRes = api.putData(authHeader, payload)
            if (putRes.isSuccessful) {
                settingsRepository.setLastSyncTime(System.currentTimeMillis())
                _syncStatus.value = SyncStatus.Success("Synced with homelab")
                Result.success(Unit)
            } else {
                if (putRes.code() == 401) {
                    settingsRepository.clearAuth()
                }
                _syncStatus.value = SyncStatus.Error("Sync failed: HTTP ${putRes.code()}")
                Result.failure(Exception("Sync failed: HTTP ${putRes.code()}"))
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: "Failed to connect to homelab")
            Result.failure(e)
        }
    }

    suspend fun pullFromServer(): Result<Unit> {
        val token = settingsRepository.authToken.firstOrNull() ?: return Result.failure(Exception("Not logged in"))
        _syncStatus.value = SyncStatus.Syncing
        val authHeader = "Bearer $token"

        return try {
            val res = api.getData(authHeader)
            if (res.isSuccessful) {
                val data = res.body()
                if (data != null && (data.sections.isNotEmpty() || data.favorites.isNotEmpty())) {
                    val favoriteNames = data.favorites.map { it.buttonName to it.pasteValue }.toSet()

                    val mapped = data.sections.mapIndexed { secIdx, sec ->
                        val sectionEntity = SectionEntity(name = sec.sectionName, sortOrder = secIdx)
                        val buttonEntities = sec.sectionButtons.mapIndexed { btnIdx, btn ->
                            val isFav = favoriteNames.contains(btn.buttonName to btn.pasteValue)
                            ButtonEntity(
                                sectionId = 0,
                                buttonName = btn.buttonName,
                                pasteValue = btn.pasteValue,
                                sortOrder = btnIdx,
                                isFavorite = isFav
                            )
                        }
                        Pair(sectionEntity, buttonEntities)
                    }
                    copyPasteDao.replaceAll(mapped)
                }
                settingsRepository.setLastSyncTime(System.currentTimeMillis())
                _syncStatus.value = SyncStatus.Success("Loaded cloud snippets")
                Result.success(Unit)
            } else {
                if (res.code() == 401) {
                    settingsRepository.clearAuth()
                }
                _syncStatus.value = SyncStatus.Error("Failed to fetch cloud snippets: HTTP ${res.code()}")
                Result.failure(Exception("HTTP ${res.code()}"))
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error(e.message ?: "Cloud sync failed")
            Result.failure(e)
        }
    }

    suspend fun login(username: String, pass: String): Result<Unit> {
        return try {
            val res = api.login(LoginRequest(username = username, password = pass))
            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                settingsRepository.setAuth(body.token, body.user.username, body.user.id)
                pullFromServer()
                Result.success(Unit)
            } else {
                val err = res.errorBody()?.string() ?: "Invalid credentials"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, pass: String): Result<Unit> {
        return try {
            val currentSections = copyPasteDao.getSections()
            val currentButtons = copyPasteDao.getButtons()
            val buttonsBySection = currentButtons.groupBy { it.sectionId }

            val payloadSections = currentSections.map { sec ->
                CopySection(
                    sectionName = sec.name,
                    sectionButtons = (buttonsBySection[sec.id] ?: emptyList()).map { btn ->
                        CopyButton(buttonName = btn.buttonName, pasteValue = btn.pasteValue)
                    }
                )
            }
            val payloadFavorites = currentButtons.filter { it.isFavorite }.mapIndexed { idx, btn ->
                FavoriteButton(buttonName = btn.buttonName, pasteValue = btn.pasteValue, sectionId = 0, buttonId = idx)
            }

            val res = api.register(
                RegisterRequest(
                    username = username,
                    password = pass,
                    initialSections = payloadSections,
                    initialFavorites = payloadFavorites
                )
            )

            if (res.isSuccessful && res.body() != null) {
                val body = res.body()!!
                settingsRepository.setAuth(body.token, body.user.username, body.user.id)
                _syncStatus.value = SyncStatus.Success("Account registered and synced")
                Result.success(Unit)
            } else {
                val err = res.errorBody()?.string() ?: "Registration failed"
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        closeLihim()
        val token = settingsRepository.authToken.firstOrNull()
        if (!token.isNullOrBlank()) {
            try {
                api.logout("Bearer $token")
            } catch (_: Exception) {
            }
        }
        settingsRepository.clearAuth()
        _syncStatus.value = SyncStatus.Idle
    }
}
