package it.craftspire.gptassist.actions

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.ui.GotItTooltip
import com.intellij.ui.scale.JBUIScale
import it.craftspire.gptassist.state.StoredStateComponent
import it.craftspire.gptassist.windows.GPTToolWindowFactory


abstract class MenuAction : AnAction() {

    companion object {
        private const val TOOLTIP_ID = "tooltip.craftspire.review"
        internal const val BACKGROUND_TASK_TITLE = "Waiting for GPT response..."
        internal const val BACKGROUND_TASK_TEXT = "Asking ChatGPT"
    }

    private val configState
        get() = StoredStateComponent.instance.state

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun getSelectedText(editor: Editor): String {
        val caretModel = editor.caretModel
        var selectedText = caretModel.currentCaret.selectedText
        if (selectedText == null) selectedText = editor.document.text
        return selectedText
    }

    private fun getFileCodingLanguage(e: AnActionEvent): Language {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val lang = file!!.language
        return lang
    }

    private fun showCodeReviewGotITTooltip(
        editor: Editor, text: String, caret: Caret
    ) = ApplicationManager.getApplication().invokeLater {
        GotItTooltip(TOOLTIP_ID, text, editor.project)
            .withShowCount(Int.MAX_VALUE)
            .withPosition(Balloon.Position.above)
            .withMaxWidth(JBUIScale.scale(800))
            .show(editor.contentComponent) { _, _ -> editor.offsetToXY(caret.selectionStart) }
    }

    private fun updateToolWindowText(text: String) = ApplicationManager.getApplication().invokeLater {
        GPTToolWindowFactory.text.append(text)
        GPTToolWindowFactory.textPane.text = GPTToolWindowFactory.text.toString()
    }

    private fun resetToolWindowText() = ApplicationManager.getApplication().invokeLater {
        GPTToolWindowFactory.text.clear()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = configState.keySet == true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val caret = e.getRequiredData(CommonDataKeys.CARET)
        val lang = getFileCodingLanguage(e)

        val selectedText = getSelectedText(editor)
        resetToolWindowText()

        val task: Task.Backgroundable = BackgroundTask(project) {
            getGPTResponse(lang, selectedText) {
                if (configState.newLayout) {
                    updateToolWindowText(it)
                } else {
                    showCodeReviewGotITTooltip(editor, it, caret)
                }
            }
        }
        task.queue()
    }

    abstract fun getGPTResponse(language: Language, selectedText: String, handleResponse: (String) -> Unit)
}

class BackgroundTask(project: Project, val task: () -> Unit) :
    Task.Backgroundable(project, MenuAction.BACKGROUND_TASK_TITLE) {

    override fun run(indicator: ProgressIndicator) {
        indicator.text = MenuAction.BACKGROUND_TASK_TEXT
        task()
    }
}