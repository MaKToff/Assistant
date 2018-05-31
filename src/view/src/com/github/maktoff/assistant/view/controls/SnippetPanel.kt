package com.github.maktoff.assistant.view.controls

import com.github.maktoff.assistant.presentationModel.ContentHolder
import com.github.maktoff.assistant.presentationModel.LanguageFacade
import com.github.maktoff.assistant.presentationModel.PsiFacade
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

class SnippetPanel : JPanel(), Disposable {
    private var editor = createEditor()
    private val info = JLabel()

    init {
        layout = BorderLayout()
        add(info, BorderLayout.NORTH)
        add(editor.component, BorderLayout.CENTER)
    }

    override fun dispose() {
        EditorFactory.getInstance().releaseEditor(editor)
    }

    fun reset(contentHolder: ContentHolder, language: String) {
        info.text = "Source: ${contentHolder.getSnippets().getLink()}"

        SwingUtilities.invokeLater {
            if (!editor.isDisposed) {
                CommandProcessor.getInstance().runUndoTransparentAction {
                    runWriteAction {
                        val lineNumber = PsiFacade.getMainLine(contentHolder, language, editor.document)
                        highlightLine(lineNumber)
                        configureEditor(language)
                    }
                }
            }
        }
    }

    private fun highlightLine(line: Int) {
        if (line >= 0) {
            val document = editor.document
            editor.selectionModel.setSelection(document.getLineStartOffset(line), document.getLineEndOffset(line))
            editor.caretModel.moveToLogicalPosition(LogicalPosition(line, 0))
        }
    }

    private fun configureEditor(language: String) {
        val extension = LanguageFacade.getExtensionByName(language)
        val fileType = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(extension)
        val scheme = EditorColorsManager.getInstance().globalScheme
        editor.highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(fileType, scheme, null)

        editor.reinitSettings()
        editor.markupModel.removeAllHighlighters()
    }

    companion object {
        private fun createEditor(): EditorEx {
            val editorFactory = EditorFactory.getInstance()
            val editorDocument = editorFactory.createDocument("")
            val editor = editorFactory.createViewer(editorDocument) as EditorEx

            editor.colorsScheme = EditorColorsManager.getInstance().globalScheme

            editor.settings.isLineMarkerAreaShown = false
            editor.settings.isIndentGuidesShown = true
            editor.settings.isFoldingOutlineShown = true

            editor.caretModel.moveToLogicalPosition(LogicalPosition(1, 1))

            return editor
        }
    }
}