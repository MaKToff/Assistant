package com.github.maktoff.assistant.model.autocompletion

import com.github.maktoff.assistant.common.Helpers
import org.json.JSONException

object AutocompletionModel {
    fun autocomplete(text: String, language: String): ArrayList<String> {
        val array = ArrayList<String>()
        var query = "http://suggestqueries.google.com/complete/search?client=firefox&hl=en&q=$language%20$text"
        query = query.replace(" ", "%20")

        try {
            var json = Helpers.getJSON(query)

            if (json.isNotEmpty()) {
                json = json.removeRange(0, json.indexOf(',') + 3)

                while (json.isNotEmpty()) {
                    val str = json.substringBefore('"')
                    array.add(str.substring(str.indexOf(" ") + 1))
                    json = json.removeRange(0, json.indexOf('"') + 3)
                }
            }
        } catch (e: Exception) {
            when (e) {
                is JSONException -> e.printStackTrace()
            }
        }

        return array
    }
}