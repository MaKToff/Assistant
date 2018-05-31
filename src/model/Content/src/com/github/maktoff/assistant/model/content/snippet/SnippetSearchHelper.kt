package com.github.maktoff.assistant.model.content.snippet

import com.github.maktoff.assistant.common.Helpers
import com.github.maktoff.assistant.model.content.Metrics
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object SnippetSearchHelper {
    private val snippets = ArrayList<Snippet>()
    private val duplicates = ArrayList<Int>()

    fun getSize() = snippets.size

    fun getSnippet(index: Int): String = snippets[index].code

    fun getLineNumber(index: Int): Int = snippets[index].lineNumber

    fun getLink(index: Int): String = snippets[index].link

    fun init(text: String, language: String, listener: SnippetListener?) {
        val keyWords = Helpers.splitToKeywords(text)
        val minValue = keyWords.size / 3
        var query = "https://searchcode.com/api/codesearch_I/?q=$text%20lang:$language&loc2=500"
        query = query.replace(" ", "%20")

        snippets.clear()
        duplicates.clear()

        try {
            val json = Helpers.getJSON(query)

            if (json.isNotEmpty()) {
                val obj = JSONObject(json)
                val array = obj.getJSONArray("results")
                for (i in 0 until array.length()) {
                    val link = array.getJSONObject(i).getString("repo")
                    val id = array.getJSONObject(i).getInt("id")
                    val lines = array.getJSONObject(i).getJSONObject("lines")
                    val keys = lines.keys()
                    val pairs = ArrayList<Pair<Int, String>>()

                    for (key in keys) {
                        pairs.add(Pair(key.toString().toInt(), lines[key.toString()].toString()))
                    }

                    addSnippet(id, link, pairs, keyWords, minValue)
                }
            }
        } catch (e: Exception) {
            when (e) {
                is JSONException -> e.printStackTrace()
            }
        }

        if (listener != null) {
            listener.maxValue = snippets.size
            listener.currentValue = 0
            listener.actionPerformed()
        }

        for (snippet in snippets) {
            snippet.code = getSnippetBy(snippet.id)

            if (listener != null) {
                listener.currentValue++
                listener.actionPerformed()
            }
        }
    }

    fun sortSnippets(code: String, keyWords: ArrayList<String>) {
        val allCodes = ArrayList<String>()

        for (snippet in snippets) {
            allCodes.add(snippet.code)
        }

        val codeVector = Metrics.getVector(code, allCodes, keyWords)

        for (snippet in snippets) {
            val vector = Metrics.getVector(snippet.code, allCodes, keyWords)
            snippet.similarity = Metrics.cosineSimilarity(codeVector, vector)
        }

        snippets.sortByDescending { snippet: Snippet ->
            snippet.similarity
        }

        while (snippets.size > 10) {
            snippets.removeAt(9)
        }
    }

    private fun addSnippet(id: Int, link: String, pairs: ArrayList<Pair<Int, String>>, keyWords: ArrayList<String>, minValue: Int) {
        val newLines = ArrayList<String>()

        var maxCount = 0
        var lineNumber = 0

        for (pair in pairs) {
            val line = pair.second
            val list = Helpers.splitToKeywords(line)

            val counter = list.count {
                keyWords.contains(it)
            }

            if (counter > maxCount) {
                maxCount = counter
                lineNumber = pair.first
            }

            newLines.add(line.trim())
        }

        if (!isDuplicate(id, newLines) && maxCount > minValue) {
            val snippet = Snippet(id, link, lineNumber, newLines)
            snippets.add(snippet)
            updateDuplicates(id.toString())
        }
    }

    private fun isDuplicate(id: Int, newLines: ArrayList<String>): Boolean {
        if (duplicates.contains(id)) {
            return true
        }

        var maxRatio = 0.0

        for (snippet in snippets) {
            val lines = snippet.lines
            var ratio = 0.0

            for (line in lines) {
                if (newLines.contains(line)) {
                    ratio++
                }
            }

            ratio /= newLines.size
            maxRatio = kotlin.math.max(ratio, maxRatio)
        }

        if (maxRatio > 0.8) {
            return true
        }

        return false
    }

    private fun updateDuplicates(id: String) {
        val query = "https://searchcode.com/api/related_results/$id/"

        try {
            val json = Helpers.getJSON(query)

            if (json.isNotEmpty()) {
                val array = JSONArray(json)

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    duplicates.add(obj.getInt("id"))
                }
            }
        } catch (e: Exception) {
            when (e) {
                is JSONException -> e.printStackTrace()
            }
        }
    }

    private fun getSnippetBy(id: Int): String {
        val query = "https://searchcode.com/api/result/$id/"
        var snippet = ""

        try {
            val json = Helpers.getJSON(query)

            if (json.isNotEmpty()) {
                val obj = JSONObject(json)
                snippet = obj.getString("code").replace("\t", "    ")
            }
        } catch (e: Exception) {
            when (e) {
                is JSONException -> e.printStackTrace()
            }
        }

        return snippet
    }

    private class Snippet(val id: Int, val link: String, val lineNumber: Int, val lines: ArrayList<String>) {
        var code = ""
        var similarity = 0.0
    }
}