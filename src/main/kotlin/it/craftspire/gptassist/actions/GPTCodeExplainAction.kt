package it.craftspire.gptassist.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import it.craftspire.gptassist.gpt.GPTRequestService


class GPTCodeExplainAction : MenuAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caret = e.getRequiredData(CommonDataKeys.CARET)

        val lang = getFileCodingLanguage(e)
        val selectedText = getSelectedText(editor)

        val gptResponse = GPTRequestService.instance.describeSelectedCode(lang.toString(), selectedText)
        showCodeReview(editor, caret, gptResponse);
    }

}