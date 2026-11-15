package online.codyuhi.copypastetool.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import online.codyuhi.copypastetool.data.db.ButtonEntity
import online.codyuhi.copypastetool.data.db.SectionEntity
import online.codyuhi.copypastetool.data.model.CopyButton
import online.codyuhi.copypastetool.data.model.CopySection
import online.codyuhi.copypastetool.data.model.FavoriteButton
import online.codyuhi.copypastetool.data.model.UserDataPayload

/**
 * In-memory home for decrypted lihim snippets. It mirrors the Room DAO operations
 * so the screens work unchanged, but nothing here is ever written to disk: the
 * server holds the only (encrypted) copy.
 */
class LihimStore {
    private val _sections = MutableStateFlow<List<SectionWithButtons>>(emptyList())
    val sections: StateFlow<List<SectionWithButtons>> = _sections.asStateFlow()

    private var nextId = 1L

    fun load(payload: UserDataPayload) {
        nextId = 1L
        val favoriteKeys = payload.favorites.map { it.buttonName to it.pasteValue }.toSet()
        _sections.value = payload.sections.mapIndexed { secIdx, sec ->
            val section = SectionEntity(id = nextId++, name = sec.sectionName, sortOrder = secIdx)
            SectionWithButtons(
                section = section,
                buttons = sec.sectionButtons.mapIndexed { btnIdx, btn ->
                    ButtonEntity(
                        id = nextId++,
                        sectionId = section.id,
                        buttonName = btn.buttonName,
                        pasteValue = btn.pasteValue,
                        sortOrder = btnIdx,
                        isFavorite = (btn.buttonName to btn.pasteValue) in favoriteKeys
                    )
                }
            )
        }
    }

    fun clear() {
        _sections.value = emptyList()
        nextId = 1L
    }

    fun toPayload(): UserDataPayload {
        val current = _sections.value
        val sections = current.map { s ->
            CopySection(
                sectionName = s.section.name,
                sectionButtons = s.buttons.map { CopyButton(buttonName = it.buttonName, pasteValue = it.pasteValue) }
            )
        }
        val favorites = mutableListOf<FavoriteButton>()
        current.forEachIndexed { secIdx, s ->
            s.buttons.forEachIndexed { btnIdx, b ->
                if (b.isFavorite) {
                    favorites += FavoriteButton(b.buttonName, b.pasteValue, sectionId = secIdx, buttonId = btnIdx)
                }
            }
        }
        return UserDataPayload(sections = sections, favorites = favorites)
    }

    fun addSection(name: String): Long {
        val id = nextId++
        _sections.update { it + SectionWithButtons(SectionEntity(id = id, name = name.trim(), sortOrder = it.size), emptyList()) }
        return id
    }

    fun updateSection(sectionId: Long, name: String) {
        _sections.update { list ->
            list.map { if (it.section.id == sectionId) it.copy(section = it.section.copy(name = name.trim())) else it }
        }
    }

    fun deleteSection(sectionId: Long) {
        _sections.update { list -> list.filterNot { it.section.id == sectionId } }
    }

    fun addButton(sectionId: Long, name: String, pasteValue: String): Long {
        val id = nextId++
        _sections.update { list ->
            list.map {
                if (it.section.id != sectionId) return@map it
                it.copy(
                    buttons = it.buttons + ButtonEntity(
                        id = id,
                        sectionId = sectionId,
                        buttonName = name.trim(),
                        pasteValue = pasteValue,
                        sortOrder = it.buttons.size
                    )
                )
            }
        }
        return id
    }

    fun updateButton(buttonId: Long, transform: (ButtonEntity) -> ButtonEntity) {
        _sections.update { list ->
            list.map { s -> s.copy(buttons = s.buttons.map { if (it.id == buttonId) transform(it) else it }) }
        }
    }

    fun deleteButton(buttonId: Long) {
        _sections.update { list -> list.map { s -> s.copy(buttons = s.buttons.filterNot { it.id == buttonId }) } }
    }

    fun favorites(sections: List<SectionWithButtons>): List<ButtonEntity> =
        sections.flatMap { it.buttons }.filter { it.isFavorite }
}
