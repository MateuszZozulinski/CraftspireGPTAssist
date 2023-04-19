package it.craftspire.gptreview.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import it.craftspire.gptreview.gpt.GPTRequestService
import it.craftspire.gptreview.state.StoredStateComponent


class GetReviewAction : AnAction() {

    private val configState
        get() = StoredStateComponent.instance.state

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)

        val lang = getFileCodingLanguage(e)
        val selectedText = getSelectedText(editor)

        val gptResponse = GPTRequestService.instance.executeCodeReviewRequest(lang.toString(), selectedText)

        showCodeReview(editor, gptResponse)
    }

    private fun getSelectedText(editor: Editor): String {
        val caretModel = editor.caretModel
        val selectedText = caretModel.currentCaret.selectedText!!
        return selectedText
    }

    private fun getFileCodingLanguage(e: AnActionEvent): Language {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val lang = file!!.language
        return lang
    }

    private fun showCodeReview(
            editor: Editor, message: String
    ) = HintManager.getInstance()
            .showInformationHint(editor, message)

    override fun update(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel = editor.getCaretModel()
        e.presentation.isEnabledAndVisible = caretModel.currentCaret.hasSelection() && configState.keySet == true
    }
}