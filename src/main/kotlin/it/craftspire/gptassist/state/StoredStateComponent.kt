package it.craftspire.gptassist.state

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage


@State(
        name = "StoredStateComponent",
        storages = [Storage("craftspire-gpt-plugin.xml")]
)
open class StoredStateComponent : PersistentStateComponent<StoredStateComponent.SavedState> {

    companion object {
        private const val CREDENTIAL_SERVICE_NAME = "GPTReview"
        val instance: StoredStateComponent
            get() = ApplicationManager.getApplication().getService(StoredStateComponent::class.java)
    }

    var savedState = SavedState()

    override fun getState(): SavedState {
        return savedState
    }

    override fun loadState(state: SavedState) {
        savedState = state
    }


    class SavedState {
        var keySet = false
        var gptModel = "gpt-3.5-turbo"
        var temperature = 0.3
        var newLayout = true

        fun setAPIKey(apiKey: String) {
            val credentialAttributes = CredentialAttributes(CREDENTIAL_SERVICE_NAME)
            PasswordSafe.instance.set(credentialAttributes, Credentials(null, apiKey))
            keySet = true
        }

        fun getAPIKey(): String? {
            val credentialAttributes = CredentialAttributes(CREDENTIAL_SERVICE_NAME)
            if (PasswordSafe.instance.get(credentialAttributes) != null) {
                val apiKey = PasswordSafe.instance.get(credentialAttributes)?.password
                return apiKey.toString()
            } else {
                return null
            }
        }
    }

}