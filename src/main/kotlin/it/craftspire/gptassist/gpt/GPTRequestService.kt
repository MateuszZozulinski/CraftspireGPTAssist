package it.craftspire.gptassist.gpt

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.knuddels.jtokkit.api.EncodingType
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import it.craftspire.gptassist.state.StoredStateComponent
import retrofit2.HttpException
import java.time.Duration


class GPTRequestService {
    private val configState
        get() = StoredStateComponent.instance.state

    companion object {
        private const val CODE_REVIEW_CONSTRUCT = "You are an expert software developer focusing on the quality of code. You will receive code snippets and tell the user what could be improved. Always respond with string in HTML format."
        private const val CODE_EXPLAIN_CONSTRUCT = "You are AI code reverse engineering expert working on explaining what the code does to novice programmers. I will provide you with code fragments in %language% and you will reply with a description of what the code is supposed to do and nothing else. " +
                "First state the purpose of this code, follow with detailed description of algorithmic steps. If there are multiple functions or steps than describe them in separately. Always respond with string in HTML format. "
        private const val MAX_LENGTH_EXCEEDED = "Sorry, your query exceeds maximum allowed length. Please select shorter text"
        private const val LANGUAGE_PATTERN = "%language%"
        private const val SYSTEM_USER = "system"
        private const val USER = "user"

        val instance: GPTRequestService
            get() = GPTRequestService()
    }

    fun reviewSelectedCode(lang: String, reviewedPatch: String, handleResponse: (String) -> Unit) {
        askGPT(CODE_REVIEW_CONSTRUCT.replace(LANGUAGE_PATTERN, lang), reviewedPatch, handleResponse)
    }

    fun describeSelectedCode(lang: String, reviewedPatch: String, handleResponse: (String) -> Unit) {
        askGPT(CODE_EXPLAIN_CONSTRUCT.replace(LANGUAGE_PATTERN, lang), reviewedPatch, handleResponse)
    }

    private fun askGPT(systemQueryConstruct: String, userQuery: String, handleResponse: (String) -> Unit) {
        if (!isValidQueryLength(userQuery)) {
            handleResponse(MAX_LENGTH_EXCEEDED)
        }
        val service = initiateOpenAIService()
        val completionRequest = buildCompletionRequest(systemQueryConstruct, userQuery)

        try {
            getGPTChatResponse(service, completionRequest, handleResponse)
        } catch (ex: HttpException) {
            "OpenAI exception occured: " + ex.message()
        }
    }

    private fun isValidQueryLength(userQuery: String): Boolean {
        val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
        val enc: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)
        return enc.countTokens(userQuery) <= 3000
    }

    private fun getGPTChatResponse(service: OpenAiService, completionRequest: ChatCompletionRequest?,
                                   handleResponse: (String) -> Unit) {
        val responseBuilder = StringBuilder("")
        if (configState.newLayout) {
            service.streamChatCompletion(completionRequest).blockingForEach {
                val response = it.choices[0].message.content
                if (response != null) handleResponse(response)
            }
        } else {
            service.createChatCompletion(completionRequest).choices.forEach {
                responseBuilder.appendLine(it.message.content)
            }
            handleResponse(responseBuilder.toString())
        }
    }

    private fun buildCompletionRequest(systemQueryConstruct: String, userQuery: String): ChatCompletionRequest? =
            ChatCompletionRequest.builder()
                    .temperature(configState.temperature)
                    .maxTokens(1024)
                    .messages(listOf(ChatMessage(
                            SYSTEM_USER, systemQueryConstruct),
                            ChatMessage(USER, userQuery)))
                    .model(configState.gptModel)
                    .stream(configState.newLayout)
                    .build()

    private fun initiateOpenAIService() = OpenAiService(configState.getAPIKey(), Duration.ofSeconds(60))
}