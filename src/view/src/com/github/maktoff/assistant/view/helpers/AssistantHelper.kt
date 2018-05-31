package com.github.maktoff.assistant.view.helpers

import com.github.maktoff.assistant.model.content.ContentType
import com.github.maktoff.assistant.model.content.discussion.Discussions
import com.github.maktoff.assistant.presentationModel.Logger
import com.github.maktoff.assistant.view.controls.SearchBar
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.ObjectUtils
import javax.swing.JPanel
import javax.swing.JTabbedPane

internal object AssistantHelper {
    fun invoke(project: Project, identifier: String, errorMessage: String, source: UITypes) {
        val assistant = ToolWindowManager.getInstance(project).getToolWindow("Assistant")
        val content = assistant.contentManager.contents[0]
        val panel = ObjectUtils.tryCast(content?.component, JPanel::class.java)
        val searchPanel = ObjectUtils.tryCast(panel?.getComponent(0), JPanel::class.java)
        val searchBar = ObjectUtils.tryCast(searchPanel?.getComponent(0), SearchBar::class.java)
        val tabbedPane = ObjectUtils.tryCast(panel?.getComponent(2), JTabbedPane::class.java)

        if (searchBar != null) {
            val manager = FileEditorManager.getInstance(project)
            val language = manager.selectedFiles[0].fileType.name.toLowerCase()
            var text = errorMessage

            if (source == UITypes.ACTION) {
                tabbedPane?.selectedIndex = 1
                Logger.searchCalled(errorMessage, language, source.value, ContentType.DISCUSSION.value)
            } else if (source == UITypes.INTENTION_ACTION) {
                text = clearText(text, '.')
                text = clearText(text, ':')

                if (hasResults(text, language)) {
                    tabbedPane?.selectedIndex = 1
                } else {
                    if (hasResults(identifier, language)) {
                        text = identifier
                    } else {
                        text = clearText(text, '\'')
                        text = clearText(text, '\"')
                        tabbedPane?.selectedIndex = 1
                    }
                }

                Logger.searchCalled(text, language, source.value, ContentType.DISCUSSION.value)
            }

            searchBar.setText(text)
            searchBar.search()
            assistant.activate(null)
        }
    }

    private fun clearText(text: String, char: Char): String {
        return if (text.contains(char))
            text.removeRange(text.indexOf(char), text.length) else text
    }

    private fun hasResults(text: String, language: String): Boolean {
        val discussions = Discussions()

        if (text.isNotEmpty()) {
            discussions.init(text, language)
        }

        return discussions.size() > 0
    }
}