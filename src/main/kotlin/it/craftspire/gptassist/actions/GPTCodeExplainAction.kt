package it.craftspire.gptassist.actions

import com.intellij.lang.Language
import it.craftspire.gptassist.gpt.GPTRequestService


class GPTCodeExplainAction : MenuAction() {

    override fun getGPTResponse(language: Language, selectedText: String): String {
        return GPTRequestService.instance.describeSelectedCode(language.toString(), selectedText)
    }

}