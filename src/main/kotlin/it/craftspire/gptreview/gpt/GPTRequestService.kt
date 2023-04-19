package it.craftspire.gptreview.gpt

import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import it.craftspire.gptreview.state.StoredStateComponent
import java.time.Duration

class GPTRequestService {
    private val configState
        get() = StoredStateComponent.instance.state

    companion object {
        val CODE_REVIEW_CONSTRUCT = "You are an AI designed to do a brief code review, outlining most important issues in code provided in %language%, check the messages for bugs or potential improvements, suggestions are welcome"
        val CODE_EXPLAIN_CONSTRUCT = "You are an AI designed to assist developers in understanding complex programming code. You will receive code in %language%, please explain it in simple words"

        val instance: GPTRequestService
            get() = GPTRequestService()
    }

    fun executeCodeReviewRequest(lang: String, reviewedPatch: String): String {
        return askGPT(CODE_REVIEW_CONSTRUCT.replace("%language%", lang), reviewedPatch)
    }

    fun describeSelectedText(lang: String, reviewedPatch: String): String {
        return askGPT(CODE_EXPLAIN_CONSTRUCT.replace("%language%", lang), reviewedPatch)
    }

    private fun askGPT(systemQueryConstruct: String, userQuery: String): String {
        val service = initiateOpenAIService()
        val completionRequest = buildCompletionRequest(systemQueryConstruct, userQuery)
        return gatherGPTResponse(service, completionRequest)
    }

    private fun gatherGPTResponse(service: OpenAiService, completionRequest: ChatCompletionRequest?): String {
        val sb = StringBuilder("")
        service.createChatCompletion(completionRequest).choices.forEach { sb.appendLine(it.message.content) }
        return sb.toString()
    }

    private fun buildCompletionRequest(systemQueryConstruct: String, userQuery: String): ChatCompletionRequest? =
            ChatCompletionRequest.builder()
                    .temperature(configState.temperature)
                    .maxTokens(2048)
                    .messages(listOf(ChatMessage(
                            "system", systemQueryConstruct),
                            ChatMessage("user", userQuery)))
                    .model(configState.gptModel)
                    .build()

    private fun initiateOpenAIService() = OpenAiService(configState.getAPIKey(), Duration.ofSeconds(60))
}