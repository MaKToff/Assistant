package com.github.maktoff.assistant.presentationModel.helpers

import com.intellij.openapi.application.PathManager

internal object Paths {
    private val path = PathManager.getPluginsPath()

    val LanguagesPath = "$path/languages.txt"
    val SettingsPath = "$path/settings.json"
    val UserPath = "$path/user.json"
}