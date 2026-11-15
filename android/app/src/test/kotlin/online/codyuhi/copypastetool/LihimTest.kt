package online.codyuhi.copypastetool

import online.codyuhi.copypastetool.data.model.CopyButton
import online.codyuhi.copypastetool.data.model.CopySection
import online.codyuhi.copypastetool.data.model.FavoriteButton
import online.codyuhi.copypastetool.data.model.UserDataPayload
import online.codyuhi.copypastetool.data.repository.LihimStore
import online.codyuhi.copypastetool.domain.LihimTapCounter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LihimTest {

    @Test
    fun tapCounterNeedsFiveTapsInsideWindow() {
        val c = LihimTapCounter(taps = 5, windowMs = 2_500)
        repeat(4) { assertFalse(c.tap(1_000L + it * 100)) }
        assertTrue(c.tap(1_500))
        // Counter resets after firing.
        assertFalse(c.tap(1_600))
    }

    @Test
    fun tapCounterDropsStaleTaps() {
        val c = LihimTapCounter(taps = 5, windowMs = 2_500)
        repeat(4) { c.tap(0L + it) }
        assertFalse(c.tap(10_000))
        repeat(3) { assertFalse(c.tap(10_001L + it)) }
        assertTrue(c.tap(10_100))
    }

    @Test
    fun storeRoundTripsPayloadAndFavorites() {
        val payload = UserDataPayload(
            sections = listOf(
                CopySection("A", listOf(CopyButton("one", "1"), CopyButton("two", "2"))),
                CopySection("B", listOf(CopyButton("three", "3")))
            ),
            favorites = listOf(FavoriteButton("two", "2", sectionId = 0, buttonId = 1))
        )
        val store = LihimStore()
        store.load(payload)
        val sections = store.sections.value
        assertEquals(listOf("A", "B"), sections.map { it.section.name })
        assertEquals(listOf("two"), store.favorites(sections).map { it.buttonName })

        val out = store.toPayload()
        assertEquals(payload.sections, out.sections)
        assertEquals(listOf(FavoriteButton("two", "2", 0, 1)), out.favorites)
    }

    @Test
    fun storeMutationsAndClear() {
        val store = LihimStore()
        val sec = store.addSection(" Hidden ")
        val btn = store.addButton(sec, "pin", "1234")
        store.updateButton(btn) { it.copy(isFavorite = true) }
        store.updateSection(sec, "Renamed")
        assertEquals("Renamed", store.sections.value.single().section.name)
        assertEquals(1, store.toPayload().favorites.size)

        store.deleteButton(btn)
        assertTrue(store.sections.value.single().buttons.isEmpty())
        store.deleteSection(sec)
        assertTrue(store.sections.value.isEmpty())

        store.addSection("x")
        store.clear()
        assertTrue(store.sections.value.isEmpty())
    }
}
