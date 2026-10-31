package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class LocalizationResourceTest {

    private data class ResourceEntry(
        val type: String,
        val value: String,
        val plurals: Map<String, String> = emptyMap()
    )

    @Test
    fun arabicResourcesMatchEnglishKeysAndTypes() {
        val english = readResources(File("src/main/res/values/strings.xml"))
        val arabic = readResources(File("src/main/res/values-ar/strings.xml"))

        assertEquals("Arabic resource keys must exactly match English", english.keys, arabic.keys)
        english.forEach { (name, source) ->
            val translated = arabic.getValue(name)
            assertEquals("Resource type mismatch for $name", source.type, translated.type)
        }
    }

    @Test
    fun arabicPluralResourcesContainRequiredQuantitiesAndFormattingArguments() {
        val english = readResources(File("src/main/res/values/strings.xml"))
        val arabic = readResources(File("src/main/res/values-ar/strings.xml"))
        val validArabicQuantities = setOf("zero", "one", "two", "few", "many", "other")

        english.filterValues { it.type == "plurals" }.forEach { (name, source) ->
            val translated = arabic.getValue(name)
            assertTrue(
                "Arabic plural $name is missing English quantity forms",
                translated.plurals.keys.containsAll(source.plurals.keys)
            )
            assertTrue(
                "Arabic plural $name contains an invalid quantity",
                validArabicQuantities.containsAll(translated.plurals.keys)
            )
            assertEquals(
                "Formatting arguments differ in plural $name",
                source.plurals.values.flatMap(::formatTokens).distinct().sorted(),
                translated.plurals.values.flatMap(::formatTokens).distinct().sorted()
            )
        }
    }

    @Test
    fun arabicStringFormattingArgumentsMatchEnglish() {
        val english = readResources(File("src/main/res/values/strings.xml"))
        val arabic = readResources(File("src/main/res/values-ar/strings.xml"))

        english.filterValues { it.type == "string" }.forEach { (name, source) ->
            assertEquals(
                "Formatting arguments differ for $name",
                formatTokens(source.value).sorted(),
                formatTokens(arabic.getValue(name).value).sorted()
            )
        }
    }

    @Test
    fun arabicLocaleContainsTranslatedUiSentinels() {
        val arabic = readResources(File("src/main/res/values-ar/strings.xml"))
        assertEquals("ابدأ التلوين", arabic.getValue("home_play_now").value)
        assertEquals("الإعدادات", arabic.getValue("settings_title").value)
        assertEquals("رجوع", arabic.getValue("navigation_back").value)
    }

    private fun readResources(file: File): Map<String, ResourceEntry> {
        assertTrue("Resource file does not exist: ${file.path}", file.isFile)
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            isXIncludeAware = false
            isExpandEntityReferences = false
        }
        val document = factory.newDocumentBuilder().parse(file)
        val root = document.documentElement
        val result = linkedMapOf<String, ResourceEntry>()
        for (index in 0 until root.childNodes.length) {
            val node = root.childNodes.item(index)
            if (node !is Element) continue
            val name = node.getAttribute("name")
            val type = node.tagName
            val entry = if (type == "plurals") {
                val items = linkedMapOf<String, String>()
                for (itemIndex in 0 until node.childNodes.length) {
                    val item = node.childNodes.item(itemIndex)
                    if (item is Element && item.tagName == "item") {
                        items[item.getAttribute("quantity")] = item.textContent
                    }
                }
                ResourceEntry(type, "", items)
            } else {
                ResourceEntry(type, node.textContent)
            }
            require(result.put(name, entry) == null) { "Duplicate resource key: $name in ${file.path}" }
        }
        return result
    }

    private fun formatTokens(value: String): List<String> =
        Regex("%(?:[0-9]+\\$)?[-#+ 0,(<]*[0-9]*(?:\\.[0-9]+)?[a-zA-Z%]")
            .findAll(value)
            .map { it.value }
            .toList()
}
