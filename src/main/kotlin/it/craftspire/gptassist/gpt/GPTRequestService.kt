package it.craftspire.gptassist.gpt

import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import it.craftspire.gptassist.state.StoredStateComponent
import java.time.Duration

class GPTRequestService {
    private val configState
        get() = StoredStateComponent.instance.state

    companion object {
        private const val CODE_REVIEW_CONSTRUCT = "You are an AI designed to do a brief code review, outlining most important issues in code provided in %language%, check the messages for bugs or potential improvements, suggestions are welcome."
        private const val CODE_EXPLAIN_CONSTRUCT = "You are an AI designed to assist developers in understanding complex programming code. You will receive code in %language%, please briefly explain the potential purpose of this code"
        private const val LANGUAGE_PATTERN = "%language%"
        private const val SYSTEM_USER = "system"
        private const val USER = "user"

        val instance: GPTRequestService
            get() = GPTRequestService()
    }

    fun reviewSelectedCode(lang: String, reviewedPatch: String): String {
        return askGPT(CODE_REVIEW_CONSTRUCT.replace(LANGUAGE_PATTERN, lang), reviewedPatch)
    }

    fun describeSelectedCode(lang: String, reviewedPatch: String): String {
        return askGPT(CODE_EXPLAIN_CONSTRUCT.replace(LANGUAGE_PATTERN, lang), reviewedPatch)
    }

    private fun askGPT(systemQueryConstruct: String, userQuery: String): String {
        val service = initiateOpenAIService()
        val completionRequest = buildCompletionRequest(systemQueryConstruct, userQuery)
        return getGPTChatResponse(service, completionRequest)
    }

    private fun getGPTChatResponse(service: OpenAiService, completionRequest: ChatCompletionRequest?): String {
        val responseBuilder = StringBuilder("")
        service.createChatCompletion(completionRequest).choices.forEach {
            responseBuilder.appendLine(it.message.content)
        }
        return responseBuilder.replace(Regex("\\n"), "<br/>").toString()
    }

    private fun buildCompletionRequest(systemQueryConstruct: String, userQuery: String): ChatCompletionRequest? =
            ChatCompletionRequest.builder()
                    .temperature(configState.temperature)
                    .maxTokens(2048)
                    .messages(listOf(ChatMessage(
                            SYSTEM_USER, systemQueryConstruct),
                            ChatMessage(USER, userQuery)))
                    .model(configState.gptModel)
                    .build()

    private fun initiateOpenAIService() = OpenAiService(configState.getAPIKey(), Duration.ofSeconds(60))
}