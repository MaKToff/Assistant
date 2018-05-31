package com.github.maktoff.assistant.presentationModel

import com.github.maktoff.assistant.model.autocompletion.AutocompletionModel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class Autocompletion(textField: JTextField) {
    private val model = DefaultComboBoxModel<String>()
    private var language = ""

    init {
        val comboBox = object : JComboBox<String>(model) {
            override fun getSize() = Dimension(textField.width + 5, 0)

            override fun getPreferredSize() = Dimension(width, 0)

            override fun getLocationOnScreen(): Point {
                val location = super.getLocationOnScreen()

                location.x -= 2
                location.y += 7

                return location
            }
        }

        comboBox.maximumRowCount = 5
        comboBox.isFocusable = false

        textField.layout = BorderLayout()
        textField.add(comboBox, BorderLayout.AFTER_LAST_LINE)

        textField.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent) {
                if (!e.isActionKey && !textField.text.isNullOrEmpty()) {
                    updateKeywords(textField.text)
                }
            }

            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_ESCAPE) {
                    comboBox.hidePopup()
                }

                if (e.keyCode == KeyEvent.VK_UP) {
                    val index = comboBox.selectedIndex

                    if (index > 0) {
                        comboBox.selectedIndex = index - 1
                    }

                    if (comboBox.selectedIndex >= 0 && comboBox.selectedItem != null) {
                        textField.text = comboBox.selectedItem.toString()
                    }
                }

                if (e.keyCode == KeyEvent.VK_DOWN) {
                    if (comboBox.isPopupVisible) {
                        val index = comboBox.selectedIndex

                        if (index < comboBox.itemCount - 1) {
                            comboBox.selectedIndex = index + 1
                        }

                        if (comboBox.selectedIndex >= 0 && comboBox.selectedItem != null) {
                            textField.text = comboBox.selectedItem.toString()
                        }
                    } else if (model.size > 1) {
                        comboBox.showPopup()
                    }
                }
            }

            override fun keyReleased(e: KeyEvent) {
                if (!e.isActionKey) {
                    if (!textField.text.isNullOrEmpty()) {
                        updateKeywords(textField.text)
                    } else {
                        comboBox.hidePopup()
                    }
                }
            }
        })

        textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                updatePopup()
            }

            override fun removeUpdate(e: DocumentEvent) {
                updatePopup()
            }

            override fun changedUpdate(e: DocumentEvent) {
                updatePopup()
            }

            private fun updatePopup() {
                comboBox.isPopupVisible = model.size > 1
            }
        })
    }

    fun updateLanguage(language: String) {
        this.language = language
    }

    private fun updateKeywords(text: String) {
        model.removeAllElements()
        model.selectedItem = -1

        val items = AutocompletionModel.autocomplete(text, language)

        for (item in items) {
            model.addElement(item)
        }
    }
}