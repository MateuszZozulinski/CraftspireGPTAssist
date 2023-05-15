package it.craftspire.gptassist.windows

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import it.craftspire.gptassist.state.StoredStateComponent
import java.awt.Font
import javax.swing.JTextPane


class GPTToolWindowFactory : ToolWindowFactory {

    companion object {
        val text = StringBuilder("")
        val textPane = JTextPane()
        private val INITIAL_TEXT = "<br/><p>Welcome to GPT Code Assist plugin. Select a code and execute action, and the response will be displayed in this window</p>"
        private val INITIAL_TEXT_SETUP = "<br/><div>" +
                "<p>Welcome to GPT Code Assist plugin. Before using the plugin you have to set it up, following these simple steps</p>" +
                "<ul>" +
                "<li>Obtain an OpenAI API key from <a href=\"https://platform.openai.com/account/api-keys\">https://platform.openai.com/account/api-keys</a> or use existing one.</li>" +
                "<li>Open the Intellij Settings Dialog, find GPT Code Assist tab and set up the key</li>" +
                "<li>Select any block of code - a new option will be available in the right-click context menu</li>" +
                "</ul>"
        private val STRIPE_TITLE = "GPT Code Assist"
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.stripeTitle = STRIPE_TITLE
        val panel = SimpleToolWindowPanel(true, true)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        textPane.contentType = "text/html"
        textPane.font = Font(null, 0, 32)
        textPane.text = if (StoredStateComponent.instance.savedState.keySet) INITIAL_TEXT_SETUP else INITIAL_TEXT
        panel.add(textPane)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return super.shouldBeAvailable(project) && StoredStateComponent.instance.savedState.newLayout
    }
}