package online.codyuhi.copypastetool.domain

object TemplateHelper {
    private val regex = Regex("""\{\{\s*([a-zA-Z0-9_\-\.]+)\s*\}\}""")

    fun extractPlaceholders(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        return regex.findAll(text).map { it.groupValues[1].trim() }.distinct().toList()
    }

    fun hasPlaceholders(text: String): Boolean {
        if (text.isEmpty()) return false
        return regex.containsMatchIn(text)
    }

    fun interpolate(text: String, variables: Map<String, String>): String {
        if (text.isEmpty()) return ""
        return regex.replace(text) { matchResult ->
            val key = matchResult.groupValues[1].trim()
            variables[key]?.takeIf { it.isNotEmpty() } ?: matchResult.value
        }
    }

    fun formatLabel(placeholder: String): String {
        return placeholder
            .replace(Regex("[_\\-]+"), " ")
            .split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }
}
