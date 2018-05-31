package com.github.maktoff.assistant.presentationModel

import com.github.maktoff.assistant.common.Helpers
import com.github.maktoff.assistant.presentationModel.helpers.Paths
import org.json.JSONObject
import java.util.*

class Settings {
    var id = ""
        private set(value) {
            field = value
            json.put("id", value)
        }

    var sendingAllowed = true
        set (value) {
            field = value
            json.put("sendingAllowed", value)
        }

    private var json = JSONObject()

    init {
        val text = Helpers.readFromFile(Paths.SettingsPath)

        if (text.isEmpty()) {
            id = UUID.randomUUID().toString()
            sendingAllowed = true
            saveSettings()
        } else {
            json = JSONObject(text)
            id = json["id"].toString()
            sendingAllowed = json["sendingAllowed"].toString().toBoolean()
        }
    }

    fun saveSettings() {
        Helpers.writeToFile(Paths.SettingsPath, json.toString(4))
    }
}