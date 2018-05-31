package com.github.maktoff.assistant.view

import com.github.maktoff.assistant.view.helpers.AssistantHelper
import com.github.maktoff.assistant.view.helpers.UITypes
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.ObjectUtils

class ErrorSearchAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project

        if (project != null) {
            val messagesToolWindow = ToolWindowManager.getInstance(project).getToolWindow("Messages")
            val component = messagesToolWindow.contentManager.contents[0].component
            val errorTreeViewPanel = ObjectUtils.tryCast(component, NewErrorTreeViewPanel::class.java)
            val element = errorTreeViewPanel?.selectedErrorTreeElement

            if (element != null) {
                AssistantHelper.invoke(project, "", element.text[0], UITypes.ACTION)
                messagesToolWindow.hide(null)
            }
        }
    }
}