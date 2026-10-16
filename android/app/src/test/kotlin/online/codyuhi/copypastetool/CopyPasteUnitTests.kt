package online.codyuhi.copypastetool

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import online.codyuhi.copypastetool.data.db.ButtonEntity
import online.codyuhi.copypastetool.data.db.SectionEntity
import online.codyuhi.copypastetool.data.model.CopyButton
import online.codyuhi.copypastetool.data.model.CopySection
import online.codyuhi.copypastetool.data.model.FavoriteButton
import online.codyuhi.copypastetool.data.model.UserDataPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CopyPasteUnitTests {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Test
    fun testUserDataPayloadSerializationParity() {
        val payload = UserDataPayload(
            sections = listOf(
                CopySection(
                    sectionName = "Greetings",
                    sectionButtons = listOf(
                        CopyButton(buttonName = "Hello", pasteValue = "Hello, world!"),
                        CopyButton(buttonName = "Bye", pasteValue = "Goodbye!")
                    )
                )
            ),
            favorites = listOf(
                FavoriteButton(buttonName = "Hello", pasteValue = "Hello, world!", sectionId = 0, buttonId = 0)
            )
        )

        val serialized = json.encodeToString(payload)
        val deserialized = json.decodeFromString<UserDataPayload>(serialized)

        assertEquals(1, deserialized.sections.size)
        assertEquals("Greetings", deserialized.sections[0].sectionName)
        assertEquals(2, deserialized.sections[0].sectionButtons.size)
        assertEquals("Hello, world!", deserialized.sections[0].sectionButtons[0].pasteValue)
        assertEquals(1, deserialized.favorites.size)
        assertEquals("Hello", deserialized.favorites[0].buttonName)
    }

    @Test
    fun testSnippetSearchFiltering() {
        val buttons = listOf(
            ButtonEntity(id = 1, sectionId = 1, buttonName = "Customer Greeting", pasteValue = "Hello! How can I help you?", sortOrder = 0),
            ButtonEntity(id = 2, sectionId = 1, buttonName = "Closing Remarks", pasteValue = "Have a great day!", sortOrder = 1),
            ButtonEntity(id = 3, sectionId = 2, buttonName = "K8s Pods", pasteValue = "kubectl get pods -A", sortOrder = 0),
        )

        // Query matching button name
        val nameQuery = "Greeting"
        val nameMatches = buttons.filter {
            it.buttonName.contains(nameQuery, ignoreCase = true) || it.pasteValue.contains(nameQuery, ignoreCase = true)
        }
        assertEquals(1, nameMatches.size)
        assertEquals("Customer Greeting", nameMatches[0].buttonName)

        // Query matching paste value
        val valueQuery = "kubectl"
        val valueMatches = buttons.filter {
            it.buttonName.contains(valueQuery, ignoreCase = true) || it.pasteValue.contains(valueQuery, ignoreCase = true)
        }
        assertEquals(1, valueMatches.size)
        assertEquals("K8s Pods", valueMatches[0].buttonName)

        // Non-matching query
        val emptyMatches = buttons.filter {
            it.buttonName.contains("nonexistent", ignoreCase = true) || it.pasteValue.contains("nonexistent", ignoreCase = true)
        }
        assertTrue(emptyMatches.isEmpty())
    }

    @Test
    fun testFavoriteToggleLogic() {
        val button = ButtonEntity(
            id = 10,
            sectionId = 1,
            buttonName = "Prompt",
            pasteValue = "Explain in detail",
            sortOrder = 0,
            isFavorite = false
        )

        val favorited = button.copy(isFavorite = true)
        assertTrue(favorited.isFavorite)

        val unfavorited = favorited.copy(isFavorite = false)
        assertFalse(unfavorited.isFavorite)
    }
}
