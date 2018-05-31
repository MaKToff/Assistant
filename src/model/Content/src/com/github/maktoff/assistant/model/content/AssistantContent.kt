package com.github.maktoff.assistant.model.content

abstract class AssistantContent {
    var currentNumber = 0
        set(value) {
            field = if (value > 0) value else 0
        }

    abstract fun init(text: String, language: String)

    abstract fun getContent(): String

    abstract fun size(): Int

    fun nextElement() {
        if (currentNumber > size()) {
            currentNumber = size()
        }

        currentNumber = when (currentNumber) {
            0 -> 0
            size() -> 1
            else -> currentNumber + 1
        }
    }

    fun previousElement() {
        currentNumber = when (currentNumber) {
            0 -> 0
            1 -> size()
            else -> currentNumber - 1
        }
    }
}