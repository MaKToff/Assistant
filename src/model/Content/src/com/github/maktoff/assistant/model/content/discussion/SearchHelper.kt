package com.github.maktoff.assistant.model.content.discussion

import com.github.maktoff.assistant.common.Helpers
import org.json.JSONException
import org.json.JSONObject

internal object SearchHelper {
    fun getLinks(text: String, tag: String, keyWords: ArrayList<String>): ArrayList<String> {
        var result = ArrayList<String>()
        var query = "http://api.stackexchange.com/2.2/search/advanced?order=desc&sort=relevance&site=stackoverflow&answers=1&tagged=$tag&title=$text"
        query = query.replace(" ", "%20")

        try {
            val json = Helpers.getJSON(query, true)

            if (json.isNotEmpty()) {
                val array = JSONObject(json).getJSONArray("items")
                val tagsArray = ArrayList<Double>()
                val reputationArray = ArrayList<Double>()
                val scoreArray = ArrayList<Double>()
                val linksArray = ArrayList<String>()

                for (i in 0 until array.length()) {
                    val jsonObject = array.getJSONObject(i)
                    val tags = jsonObject.getJSONArray("tags")
                    val owner = jsonObject.getJSONObject("owner")

                    if (!owner.has("reputation")) {
                        continue
                    }

                    val reputation = owner.getDouble("reputation")
                    val score = jsonObject.getDouble("score")
                    val link = jsonObject.getString("link")

                    var tagsCounter = 0.0

                    for (j in 0 until tags.length()) {
                        if (keyWords.contains(tags[j].toString())) {
                            tagsCounter++
                        }
                    }

                    tagsArray.add(tagsCounter / keyWords.size)
                    reputationArray.add(reputation)
                    scoreArray.add(score)
                    linksArray.add(link)
                }

                result = sortDiscussions(tagsArray, reputationArray, scoreArray, linksArray)
            }
        } catch (e: Exception) {
            when (e) {
                is JSONException -> e.printStackTrace()
            }
        }

        return result
    }

    private fun normalize(average: Double, list: ArrayList<Double>) {
        for (i in 0 until list.size) {
            list[i] = 1 / (1 + kotlin.math.exp((average - list[i])))
        }
    }

    private fun sortDiscussions(tags: ArrayList<Double>, reputation: ArrayList<Double>, score: ArrayList<Double>, links: ArrayList<String>): ArrayList<String> {
        val tagWeight = 0.18
        val reputationWeight = 0.13
        val scoreWeight = 0.07
        val multiplier = 1.0 / (tagWeight + reputationWeight + scoreWeight)

        // The average reputation for all users in Stack Overflow
        // http://data.stackexchange.com/stackoverflow/query/261020
        val averageReputation = 112.0

        // The average score for all questions in Stack Overflow
        // http://data.stackexchange.com/stackoverflow/query/833263
        val averageScore = 2.0

        normalize(averageReputation, reputation)
        normalize(averageScore, score)

        val pairs = ArrayList<Pair<String, Double>>()

        for (i in 0 until links.size) {
            val total = multiplier * (tags[i] * tagWeight + reputation[i] * reputationWeight + score[i] * scoreWeight)

            if (links[i].isNotEmpty()) {
                pairs.add(Pair(links[i], total))
            }
        }

        pairs.sortByDescending { pair: Pair<String, Double> ->
            pair.second
        }

        val result = ArrayList<String>()

        for (item in pairs) {
            if (result.size >= 10) {
                break
            }

            result.add(item.first)
        }

        return result
    }
}