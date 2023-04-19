package it.craftspire.gptreview.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import it.craftspire.gptreview.state.StoredStateComponent
import java.time.Duration
import kotlin.text.StringBuilder


class GetReviewAction : AnAction() {
    private val configState
        get() = StoredStateComponent.instance.state

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val lang = file!!.language
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel = editor.caretModel
        val selectedText = caretModel.currentCaret.selectedText

        val service = OpenAiService(configState.getAPIKey(), Duration.ofSeconds(60))

        val completionRequest = ChatCompletionRequest.builder()
                .temperature(0.3)
                .maxTokens(2048)
                .messages(listOf(ChatMessage(
                        "system", "You are an AI designed to do a brief code review, outlining most important issues in code provided in $lang "),
                        ChatMessage("user", "below is code fragment, please provide me a brief code review, " +
                                "each response line should single issue in format [linenumber:severity] issue:  \n$selectedText")))
                .model("gpt-3.5-turbo")
                .build()
        val sb = StringBuilder("")
        service.createChatCompletion(completionRequest).choices.forEach{ sb.appendLine(it.message)}
        showCodeReview(editor, sb.toString())
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