package online.codyuhi.copypastetool

import online.codyuhi.copypastetool.domain.TemplateHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateHelperTest {

    @Test
    fun testExtractPlaceholders() {
        assertEquals(emptyList<String>(), TemplateHelper.extractPlaceholders("Static text"))
        assertEquals(listOf("name"), TemplateHelper.extractPlaceholders("Hello {{name}}!"))
        assertEquals(
            listOf("name", "ticket_id", "env"),
            TemplateHelper.extractPlaceholders("Hello {{ name }}, ticket {{ ticket_id }} in {{env}}.")
        )
        // Deduplication
        assertEquals(
            listOf("user"),
            TemplateHelper.extractPlaceholders("{{user}} talking to {{user}}")
        )
    }

    @Test
    fun testHasPlaceholders() {
        assertFalse(TemplateHelper.hasPlaceholders("Plain static"))
        assertTrue(TemplateHelper.hasPlaceholders("Hello {{name}}"))
        assertTrue(TemplateHelper.hasPlaceholders("Key {{ key_id }}"))
    }

    @Test
    fun testInterpolate() {
        val template = "Deploy {{service}} to {{env}} namespace {{ns}}."
        val vars = mapOf(
            "service" to "auth-api",
            "env" to "production",
            "ns" to "apps"
        )
        val result = TemplateHelper.interpolate(template, vars)
        assertEquals("Deploy auth-api to production namespace apps.", result)
    }

    @Test
    fun testFormatLabel() {
        assertEquals("Customer Name", TemplateHelper.formatLabel("customer_name"))
        assertEquals("Incident Id", TemplateHelper.formatLabel("incident-id"))
        assertEquals("Namespace", TemplateHelper.formatLabel("namespace"))
    }
}
