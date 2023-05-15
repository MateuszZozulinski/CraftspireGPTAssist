package it.craftspire.gptassist.actions

import com.intellij.lang.Language
import it.craftspire.gptassist.gpt.GPTRequestService


class GPTReviewAction : MenuAction() {
    override fun getGPTResponse(language: Language, selectedText: String, handleResponse: (String) -> Unit) {
        GPTRequestService.instance.reviewSelectedCode(language.toString(), selectedText, handleResponse)
    }

}