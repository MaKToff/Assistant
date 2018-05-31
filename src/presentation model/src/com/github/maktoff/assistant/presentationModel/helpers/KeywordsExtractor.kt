package com.github.maktoff.assistant.presentationModel.helpers

import com.github.maktoff.assistant.common.Helpers
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

internal class KeywordsExtractor(private val fileManager: FileEditorManager, private val psiManager: PsiManager) {
    fun getSourceCode(): String {
        val psiFile = if (fileManager.selectedFiles.isNotEmpty())
            psiManager.findFile(fileManager.selectedFiles[0]) else null

        return if (psiFile != null) psiFile.text else ""
    }

    fun getKeywordsFormImports(query: String): ArrayList<String> {
        val keyWords = ArrayList<String>()
        val psiFile = if (fileManager.selectedFiles.isNotEmpty())
            psiManager.findFile(fileManager.selectedFiles[0]) else null

        val splittedQuery = query.split(" ")

        for (word in splittedQuery) {
            if (word.isNotEmpty() && !keyWords.contains(word)) {
                keyWords.add(word)
            }
        }

        if (psiFile != null) {
            val children = psiFile.children

            for (child in children) {
                if (child is PsiImportList) {
                    val identifiers = extractIdentifiers(child)

                    for (item in identifiers) {
                        val word = item.toLowerCase()

                        if (word.isNotEmpty() && !keyWords.contains(word)) {
                            keyWords.add(word)
                        }
                    }

                    break
                }
            }
        }

        return Helpers.preProcessKeyWords(keyWords)
    }

    fun getKeywordsFromNames(): ArrayList<String> {
        val keyWords = ArrayList<String>()
        val psiFile = if (fileManager.selectedFiles.isNotEmpty())
            psiManager.findFile(fileManager.selectedFiles[0]) else null

        if (psiFile != null) {
            val elements = PsiTreeUtil.collectElements(psiFile, {
                it is PsiClass
            })

            for (element in elements) {
                val identifiers = extractIdentifiers(element)

                for (item in identifiers) {
                    val list = Helpers.splitCamelCase(item)

                    for (word in list) {
                        if (word.isNotEmpty() && !keyWords.contains(word)) {
                            keyWords.add(word)
                        }
                    }
                }
            }
        }

        return Helpers.preProcessKeyWords(keyWords)
    }

    private fun extractIdentifiers(element: PsiElement): ArrayList<String> {
        val result = ArrayList<String>()
        val identifiers = PsiTreeUtil.collectElements(element, {
            it is PsiIdentifier
        })

        for (item in identifiers) {
            val text = item.text

            if (!text.isNullOrEmpty() && !result.contains(text)) {
                result.add(text)
            }
        }

        return result
    }
}