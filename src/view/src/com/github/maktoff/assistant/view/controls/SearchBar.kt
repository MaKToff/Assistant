package com.github.maktoff.assistant.view.controls

import com.github.maktoff.assistant.presentationModel.Autocompletion
import com.github.maktoff.assistant.view.helpers.Icons
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.event.*
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.CompoundBorder

class SearchBar : JPanel(BorderLayout()) {
    private val searchLabel = JLabel(Icons.Search)
    private val textField = HintTextField()
    private var searchAction = Runnable { }
    private val autocompletion = Autocompletion(textField)

    init {
        border = CompoundBorder(JBUI.Borders.empty(0, 0, 0, 0), textField.border)

        searchLabel.background = textField.background

        textField.isOpaque = true
        textField.border = JBUI.Borders.empty(0, 5, 0, 0)
        textField.background = background

        add(searchLabel, BorderLayout.WEST)
        add(textField, BorderLayout.CENTER)

        searchLabel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                search()
            }

            override fun mouseEntered(e: MouseEvent?) {
                searchLabel.icon = Icons.SearchHover
            }

            override fun mouseExited(e: MouseEvent?) {
                searchLabel.icon = Icons.Search
            }
        })

        textField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    searchAction.run()
                }
            }
        })
    }

    override fun hasFocus() = textField.hasFocus()

    fun search() = searchAction.run()

    fun setSearchAction(function: Runnable) {
        searchAction = function
    }

    fun getText(): String = textField.text

    fun setText(text: String) {
        textField.text = text
    }

    fun updateLanguage(language: String) = autocompletion.updateLanguage(language)

    private inner class HintTextField : JTextField(), FocusListener {
        private val placeholder = "How can I do ..."

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)

            if (!hasFocus() && text.isEmpty()) {
                g.font = g.font.deriveFont(Font.ITALIC)
                g.color = UIUtil.getInactiveTextColor()

                val fontHeight = g.fontMetrics.height
                val height = (height - fontHeight) / 2 + fontHeight - 4
                val g2d = g as Graphics2D

                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                g2d.drawString(placeholder, insets.left, height)
            }
        }

        override fun focusGained(e: FocusEvent) = repaint()

        override fun focusLost(e: FocusEvent) = repaint()
    }
}