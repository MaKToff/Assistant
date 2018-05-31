package com.github.maktoff.assistant.presentationModel

import com.github.maktoff.assistant.model.content.AssistantContent
import com.github.maktoff.assistant.model.content.ContentType
import com.github.maktoff.assistant.model.content.discussion.Discussions
import com.github.maktoff.assistant.model.content.snippet.Snippets
import com.github.maktoff.assistant.presentationModel.helpers.KeywordsExtractor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiFileFactoryImpl
import java.util.concurrent.Executors

class ContentHolder(project: Project) {
    var contentType = ContentType.SNIPPET
        set(value) {
            field = value
            resetContent()
        }

    private val snippets = Snippets()
    private val discussions = Discussions()
    private var content: AssistantContent = snippets

    private val fileManager = FileEditorManager.getInstance(project)
    private val psiManager = PsiManager.getInstance(project)
    private val psiFileFactory = PsiFileFactoryImpl.getInstance(project)
    private val extractor = KeywordsExtractor(fileManager, psiManager)

    fun reset(text: String, language: String, loadSnippets: Runnable, loadDiscussions: Runnable) {
        snippets.setContext(extractor.getSourceCode(), extractor.getKeywordsFromNames())
        discussions.setContext(extractor.getKeywordsFormImports(text))

        val executor = Executors.newCachedThreadPool()
        executor.submit({ loadContent(snippets, text, language, loadSnippets) })
        executor.submit({ loadContent(discussions, text, language, loadDiscussions) })
    }

    fun previousElement() = content.previousElement()

    fun nextElement() = content.nextElement()

    fun getSize(): Int = content.size()

    fun getCurrentNumber(): Int = content.currentNumber

    fun resetLanguage(): String {
        var language = ""

        if (fileManager.selectedFiles.isNotEmpty()) {
            val fileType = fileManager.selectedFiles[0].fileType
            language = LanguageFacade.getNameByType(fileType)
        }

        return language
    }

    fun getSnippets() = snippets

    fun getDiscussions() = discussions

    internal fun getFileFactory(): PsiFileFactory = psiFileFactory

    private fun resetContent() {
        content = when (contentType) {
            ContentType.SNIPPET -> snippets
            ContentType.DISCUSSION -> discussions
        }
    }

    private fun loadContent(content: AssistantContent, text: String, language: String, function: Runnable) {
        content.init(text, language)
        function.run()
    }
}