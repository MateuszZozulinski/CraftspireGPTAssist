package it.craftspire.gptassist.actions

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.ui.GotItTooltip
import it.craftspire.gptassist.state.StoredStateComponent


abstract class MenuAction : AnAction() {

    companion object {
        private const val TOOLTIP_ID = "tooltip.craftspire.review"
    }

    private val configState
        get() = StoredStateComponent.instance.state

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    internal fun getSelectedText(editor: Editor): String {
        val caretModel = editor.caretModel
        val selectedText = caretModel.currentCaret.selectedText!!
        return selectedText
    }

    internal fun getFileCodingLanguage(e: AnActionEvent): Language {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val lang = file!!.language
        return lang
    }

    internal fun showCodeReview(
            editor: Editor, caret: Caret, text: String
    ) = GotItTooltip(TOOLTIP_ID, text, editor.project)
            .withShowCount(Int.MAX_VALUE)
            .withPosition(Balloon.Position.above)
            .withMaxWidth(800)
            .show(editor.contentComponent) { c, b -> editor.offsetToXY(caret.selectionStart) }

    override fun update(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel = editor.getCaretModel()
        e.presentation.isEnabledAndVisible = caretModel.currentCaret.hasSelection() && configState.keySet == true
    }
}