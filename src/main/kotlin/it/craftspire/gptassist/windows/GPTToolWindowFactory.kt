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
        val textPane = JTextPane()
        private val INITIAL_TEXT = "<br/><p>Welcome to GPT Code Assist plugin. Select a code and execute action, and the response will be displayed in this window</p>"
        private val STRIPE_TITLE = "GPT Code Assist"
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.stripeTitle = STRIPE_TITLE
        val panel = SimpleToolWindowPanel(true, true)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        textPane.contentType = "text/html"
        textPane.font = Font(null, 0, 32)
        textPane.text = INITIAL_TEXT
        panel.add(textPane)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return super.shouldBeAvailable(project) && StoredStateComponent.instance.savedState.newLayout
    }
}