package com.github.maktoff.assistant.view.controls

import com.intellij.openapi.actionSystem.AnAction
import javax.swing.Icon

internal abstract class ActionButton(description: String, icon: Icon) : AnAction("", description, icon) {
    var isActive = false

    override fun displayTextInToolbar() = true
}