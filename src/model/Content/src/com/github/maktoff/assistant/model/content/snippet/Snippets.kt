package com.github.maktoff.assistant.model.content.snippet

import com.github.maktoff.assistant.model.content.AssistantContent

class Snippets : AssistantContent() {
    private var code = ""
    private val keyWords = ArrayList<String>()
    private var listener: SnippetListener? = null

    override fun init(text: String, language: String) {
        currentNumber = 0
        SnippetSearchHelper.init(text, language, listener)

        val size = SnippetSearchHelper.getSize()

        if (size != 0) {
            currentNumber = 1
            SnippetSearchHelper.sortSnippets(code, keyWords)
        }
    }

    override fun getContent() = SnippetSearchHelper.getSnippet(currentNumber - 1)

    override fun size(): Int = SnippetSearchHelper.getSize()

    fun getLineNumber() = SnippetSearchHelper.getLineNumber(currentNumber - 1)

    fun getLink() = SnippetSearchHelper.getLink(currentNumber - 1)

    fun setContext(code: String, keyWords: ArrayList<String>) {
        this.code = code
        this.keyWords.clear()
        this.keyWords.addAll(keyWords)
    }

    fun addListener(listener: SnippetListener) {
        this.listener = listener
    }
}