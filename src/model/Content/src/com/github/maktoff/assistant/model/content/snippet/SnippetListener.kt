package com.github.maktoff.assistant.model.content.snippet

abstract class SnippetListener {
    var maxValue = 0
    var currentValue = 0

    open fun actionPerformed() {
    }
}