package com.github.maktoff.assistant.presentationModel

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

object PsiFacade {
    fun getMainLine(contentHolder: ContentHolder, language: String, document: Document): Int {
        var psiFile: PsiFile? = null
        val snippets = contentHolder.getSnippets()
        val psiFileFactory = contentHolder.getFileFactory()
        val text = snippets.getContent()
        val line = snippets.getLineNumber()

        runReadAction {
            psiFile = psiFileFactory.createFileFromText(LanguageFacade.getLanguageByName(language), text)
        }

        document.replaceString(0, document.textLength, StringUtil.convertLineSeparators(text))

        val bounds = findBounds(psiFile, document, line)
        var lineNumber = line - 1

        if (bounds.first != 0 || bounds.second != 0) {
            lineNumber = line - document.getLineNumber(bounds.first) - 1

            var snippet = text.removeRange(bounds.second, text.length).removeRange(0, bounds.first)
            val startIndex = snippet.lastIndexOf('\n') + 1
            val indent = snippet.subSequence(startIndex, snippet.length - 1).toString()

            snippet = snippet.replace("\n" + indent, "\n")
            snippet = snippet.replace(indent, "  ")

            document.replaceString(0, document.textLength, StringUtil.convertLineSeparators(snippet))
        }

        return lineNumber
    }

    fun isError(element: PsiElement): Boolean {
        val parent = element.parent

        if (parent is PsiErrorElement || checkReferences(parent)) {
            return true
        }

        var children = parent.children

        if (children.size == 1) {
            children = parent.parent.children
        }

        for (child in children) {
            if (child is PsiErrorElement || checkReferences(child)) {
                return true
            }
        }

        return false
    }

    private fun findBounds(psiFile: PsiFile?, document: Document, line: Int): Pair<Int, Int> {
        val methods = PsiTreeUtil.collectElements(psiFile, {
            it is PsiMethod
        })

        var currentMethod: PsiElement? = null

        if (psiFile != null) {
            for (method in methods) {
                val offset = method.firstChild.textOffset
                val lineNumber = document.getLineNumber(offset)

                if (lineNumber <= line) {
                    currentMethod = method
                } else {
                    break
                }
            }
        }

        var left = 0
        var right = 0

        if (currentMethod != null) {
            left = currentMethod.firstChild.textOffset

            val blocks = PsiTreeUtil.collectElements(currentMethod, {
                it is PsiCodeBlock
            })

            for (block in blocks) {
                right = kotlin.math.max(block.lastChild.textOffset + 1, right)
            }
        }

        if (left > right) {
            left = 0
            right = 0
        }

        return Pair(left, right)
    }

    private fun checkReferences(element: PsiElement): Boolean {
        val references = element.references

        for (reference in references) {
            if (reference.resolve() == null) {
                return true
            }
        }

        return false
    }
}