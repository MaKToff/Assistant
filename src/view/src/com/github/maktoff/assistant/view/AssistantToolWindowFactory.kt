package com.github.maktoff.assistant.view

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class AssistantToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = AssistantToolWindow(project).toolWindowContent
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(toolWindowContent, "", false)

        toolWindow.isAutoHide = false

        contentManager.addContent(content)
    }
}