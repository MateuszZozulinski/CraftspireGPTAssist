package it.craftspire.gptreview.dialogs

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import com.intellij.util.ui.FormBuilder
import it.craftspire.gptreview.state.StoredStateComponent
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.swing.JComponent
import javax.swing.JFormattedTextField
import javax.swing.JPanel
import javax.swing.JTextField


class ConfigDialog : Configurable, Disposable {

    private val temperatureFormatter = DecimalFormat.getInstance().also {
        it.minimumFractionDigits = 2
        it.maximumFractionDigits = 2
        it.roundingMode = RoundingMode.HALF_UP
    }

    private val configState
        get() = StoredStateComponent.instance.state

    private var gptModelField: JTextField? =
            JTextField(40).also { it.text = configState.gptModel }
    private var gptApiKeyField: JTextField? =
            JTextField(40).also { it.text = "***" }

    private var temperatureField: JFormattedTextField? =
            JFormattedTextField(temperatureFormatter).also { it.value = configState.temperature }

    override fun getDisplayName(): String = "Craftspire GPT Code Review Plugin Configuration"

    override fun createComponent(): JComponent {

        val formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("ChatGPT API Key", JPanel(FlowLayout(FlowLayout.LEFT)).also {
                    it.add(gptApiKeyField)
                })
                .addLabeledComponent("GPT model", JPanel(FlowLayout(FlowLayout.LEFT)).also {
                    it.add(gptModelField)
                })
                .addLabeledComponent("GPT temperature", JPanel(FlowLayout(FlowLayout.LEFT)).also {
                    it.add(temperatureField)
                })
                .panel

        return JPanel(BorderLayout()).also { it.add(formPanel, BorderLayout.NORTH) }
    }


    override fun dispose() {
        gptApiKeyField = null
        gptModelField = null
        temperatureField = null
    }

    override fun isModified(): Boolean {
        return configState.gptModel != gptModelField!!.text
                || "***" != gptApiKeyField!!.text
                || configState.temperature != temperatureField!!.text.toDoubleOrNull()
    }

    override fun apply() {
        gptApiKeyField?.let {
            if ("***" != it.text) {
                configState.setAPIKey(it.text)
                it.text = "***"
            }
        }

        configState.gptModel = gptModelField!!.text

        temperatureField?.let { field ->
            temperatureFormatter.parse(field.text).toDouble().let {
                if (it in 0.0..1.0) {
                    configState.temperature = it
                }
            }
        }
    }

    override fun reset() {
        gptApiKeyField!!.text = "***"
        gptModelField!!.text = configState.gptModel
        temperatureField!!.value = configState.temperature
    }
}