package it.craftspire.gptreview.state

import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.remoteServer.util.CloudConfigurationUtil.createCredentialAttributes
import java.util.*


@State(
        name = "ReminderPersistentStateComponent",
        storages = [Storage("craftspire-gpt-plugin.xml")]
)
open class StoredStateComponent : PersistentStateComponent<StoredStateComponent.SavedState> {

    companion object {
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
        val credentialsKey = UUID.randomUUID();
        var keySet = false;
        var gptModel = "gpt-3.5-turbo";
        var temperature = 0.3;

        fun setAPIKey(apiKey: String) {
            val credentialAttributes = createCredentialAttributes("GPT Review", credentialsKey.toString())!!
            val credentials = Credentials(apiKey)
            PasswordSafe.instance.set(credentialAttributes, credentials);
            keySet = true;
        }

        fun getAPIKey(): String? {
            val credentialAttributes = createCredentialAttributes("GPT Review", credentialsKey.toString())!!
            return PasswordSafe.instance.getPassword(credentialAttributes);
        }
    }

}