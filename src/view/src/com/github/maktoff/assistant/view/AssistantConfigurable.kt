package com.github.maktoff.assistant.view

import com.github.maktoff.assistant.presentationModel.Settings
import com.intellij.openapi.options.Configurable
import com.intellij.ui.IdeBorderFactory
import java.awt.BorderLayout
import javax.swing.JCheckBox
import javax.swing.JPanel

class AssistantConfigurable : Configurable {
    private val sendingAllowed = JCheckBox("Allow sending anonymous statistics", true)
    private val settings = Settings()

    override fun createComponent(): JPanel {
        val panel = JPanel()

        panel.layout = BorderLayout()

        sendingAllowed.setMnemonic('A')
        sendingAllowed.isSelected = settings.sendingAllowed

        val settingsPanel = JPanel(BorderLayout())
        settingsPanel.border = IdeBorderFactory.createTitledBorder("SettingsPath", true)
        settingsPanel.add(sendingAllowed, BorderLayout.NORTH)

        panel.add(settingsPanel, BorderLayout.NORTH)

        return panel
    }

    override fun apply() {
        settings.sendingAllowed = sendingAllowed.isSelected
        settings.saveSettings()
    }

    override fun getDisplayName() = "Assistant plugin"

    override fun isModified(): Boolean {
        if (sendingAllowed.isSelected != settings.sendingAllowed) {
            return true
        }

        return false
    }

    override fun reset() {
        sendingAllowed.isSelected = settings.sendingAllowed
    }

    override fun getHelpTopic(): String? = ""
}