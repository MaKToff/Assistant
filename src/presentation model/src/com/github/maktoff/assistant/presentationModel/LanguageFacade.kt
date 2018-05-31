package com.github.maktoff.assistant.presentationModel

import com.github.maktoff.assistant.common.Helpers
import com.github.maktoff.assistant.presentationModel.helpers.Paths
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import java.util.*

object LanguageFacade {
    private const val ANY = "<any>"

    private val languages = mutableMapOf(
            Pair(ANY, ""),
            Pair("java", ".java"),
            Pair("kotlin", ".kt")
    )

    init {
        val text = Helpers.readFromFile(Paths.LanguagesPath)

        for (key in languages.keys) {
            if (!text.contains(key)) {
                Helpers.writeToFile(Paths.LanguagesPath, key + System.lineSeparator(), true)
            }
        }
    }

    fun getAllLanguages(): MutableList<String> = Collections.unmodifiableList(languages.keys.toList())

    fun getLanguageByName(language: String): Language {
        val list = Language.getRegisteredLanguages()
        var id = ""

        for (item in list) {
            if (item.id.toLowerCase() == language) {
                id = item.id
            }
        }

        return Language.findLanguageByID(id) ?: Language.ANY
    }

    fun getNameByType(fileType: FileType): String {
        var language = fileType.name.toLowerCase()

        if (language.contains("_")) {
            language = ANY
        } else if (!languages.contains(language)) {
            languages[language] = fileType.defaultExtension
            Helpers.writeToFile(Paths.LanguagesPath, language + System.lineSeparator(), true)
        }

        return language
    }

    fun getExtensionByName(language: String): String = languages[language] ?: ""
}