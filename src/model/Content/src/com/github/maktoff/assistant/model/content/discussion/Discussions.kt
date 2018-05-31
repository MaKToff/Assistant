package com.github.maktoff.assistant.model.content.discussion

import com.github.maktoff.assistant.model.content.AssistantContent

class Discussions : AssistantContent() {
    private val links: ArrayList<String> = ArrayList()
    private val keyWords = ArrayList<String>()

    override fun init(text: String, language: String) {
        links.clear()
        currentNumber = 0

        val array = SearchHelper.getLinks(text, language, keyWords)

        if (array.isNotEmpty()) {
            fill(array)
        }
    }

    override fun getContent(): String {
        if (currentNumber == 0) {
            return ""
        }

        return links[currentNumber - 1]
    }

    override fun size(): Int = links.size

    fun setContext(keyWords: ArrayList<String>) {
        this.keyWords.clear()
        this.keyWords.addAll(keyWords)
    }

    private fun fill(array: ArrayList<String>) {
        currentNumber = if (array.size > 0) 1 else 0

        for (i in 0 until array.size) {
            links.add(array[i])
        }
    }
}