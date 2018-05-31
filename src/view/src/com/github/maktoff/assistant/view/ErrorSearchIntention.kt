package com.github.maktoff.assistant.view

import com.github.maktoff.assistant.presentationModel.PsiFacade
import com.github.maktoff.assistant.view.helpers.AssistantHelper
import com.github.maktoff.assistant.view.helpers.UITypes
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement

class ErrorSearchIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getText(): String = "More information"

    override fun getFamilyName(): String = text

    override fun startInWriteAction(): Boolean = false

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        if (!element.isWritable) {
            return false
        }

        if (PsiFacade.isError(element)) {
            return true
        }

        return false
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val parent = element.parent
        val children = parent.children

        for (child in children) {
            if (child is PsiErrorElement) {
                AssistantHelper.invoke(project, child.text, child.errorDescription, UITypes.INTENTION_ACTION)
                return
            }

            val statusBar = WindowManager.getInstance().getStatusBar(project)
            val message = if (statusBar.info != null) statusBar.info else ""
            val name = element.text

            AssistantHelper.invoke(project, name, message, UITypes.INTENTION_ACTION)
        }
    }
}