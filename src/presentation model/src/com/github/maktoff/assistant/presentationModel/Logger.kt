package com.github.maktoff.assistant.presentationModel

import com.github.maktoff.assistant.common.Helpers
import com.github.maktoff.assistant.presentationModel.helpers.Paths
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

object Logger {
    private var json = JSONObject()
    private var id = ""

    init {
        init()
    }

    fun load(id: String) {
        val text = Helpers.readFromFile(Paths.UserPath)
        this.id = id

        if (text.isEmpty()) {
            init()
            save()
        } else {
            json = JSONObject(text)

            if (json["id"].toString().isEmpty()) {
                json.put("id", id)
            }

            save()
        }
    }

    fun searchCalled(query: String, language: String, source: String, contentType: String) {
        val array = json.getJSONArray("actions")
        val obj = JSONObject()

        obj.put("query", query)
        obj.put("language", language)
        obj.put("source", source)
        obj.put("clicks", JSONArray())
        array.put(obj)

        clicked(1, contentType)
    }

    fun clicked(index: Int, contentType: String) {
        val array = json.getJSONArray("actions")

        if (array.length() > 0) {
            val clicks = array.getJSONObject(array.length() - 1).getJSONArray("clicks")
            val obj = JSONObject()

            obj.put("index", index)
            obj.put("contentType", contentType)
            obj.put("time", LocalDateTime.now())
            obj.put("helpful", false)
            clicks.put(obj)
        }

        save()
    }

    fun favouriteClicked(index: Int, contentType: String) {
        val array = json.getJSONArray("actions")

        if (array.length() > 0) {
            val clicks = array.getJSONObject(array.length() - 1).getJSONArray("clicks")

            for (i in 0 until clicks.length()) {
                val obj = clicks.getJSONObject(i)

                if (obj["index"] == index && obj["contentType"] == contentType) {
                    obj.put("helpful", true)
                }
            }
        }

        save()
    }

    private fun init() {
        json = JSONObject()

        json.put("id", id)
        json.put("actions", JSONArray())
    }

    private fun save() {
        // Logging is now disabled.
        //Helpers.writeToFile(Paths.UserPath, json.toString(4))
    }
}