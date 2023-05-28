package it.craftspire.gptassist.dialogs

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.layout.selectedValueIs
import com.intellij.util.ui.FormBuilder
import it.craftspire.gptassist.state.StoredStateComponent
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import javax.swing.*


class ConfigDialog : Configurable, Disposable {

    companion object {
        private const val HIDDEN_API_TOKEN = "***"
    }

    private val temperatureFormatter = DecimalFormat.getInstance().also {
        it.minimumFractionDigits = 2
        it.maximumFractionDigits = 2
        it.roundingMode = RoundingMode.HALF_UP
    }

    private val configState
        get() = StoredStateComponent.instance.state

    private var gptModelField: ComboBox<String>? =
            ComboBox<String>().also {
                it.columns(30)
                it.addItem("gpt-3.5-turbo")
                it.addItem("gpt-4")
                it.addItem("gpt-4-32k")
                it.selectedItem = configState.gptModel
            }

    private var gptApiKeyField: JTextField? =
            JTextField(30).also { it.text = HIDDEN_API_TOKEN }

    private var temperatureField: JFormattedTextField? =
            JFormattedTextField(temperatureFormatter).also { it.value = configState.temperature }

    private var newLayoutCheckbox: JCheckBox? = JCheckBox().also { it.isSelected = configState.newLayout }

    override fun getDisplayName(): String = "Craftspire GPT Code Assist Plugin Configuration"

    override fun createComponent(): JComponent {

        val formPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent("OpenAI API Key", JPanel(FlowLayout(FlowLayout.LEFT)).also {
                    it.add(gptApiKeyField)
                })
                .addLabeledComponent("GPT model", JPanel(FlowLayout(FlowLayout.LEFT)).also {
                    it.add(gptModelField)
                })
                .addLabeledComponent("GPT temperature", JPanel(FlowLayout(FlowLayout.LEFT)).also {
                    it.add(temperatureField)
                })
                .addLabeledComponent("Stream GPT responses (requires restart)", JPanel(FlowLayout(FlowLayout.LEFT)).also {
                    it.add(newLayoutCheckbox)
                })
                .panel

        return JPanel(BorderLayout()).also { it.add(formPanel, BorderLayout.NORTH) }
    }


    override fun dispose() {
        gptApiKeyField = null
        gptModelField = null
        temperatureField = null
        newLayoutCheckbox = null
    }

    override fun isModified(): Boolean {
        return configState.gptModel != gptModelField!!.selectedItem!!.toString()
                || HIDDEN_API_TOKEN != gptApiKeyField!!.text
                || configState.temperature != temperatureField!!.text.toDoubleOrNull()
                || configState.newLayout != newLayoutCheckbox!!.isSelected
    }

    override fun apply() {
        gptApiKeyField?.let {
            if (HIDDEN_API_TOKEN != it.text) {
                configState.setAPIKey(it.text)
                it.text = HIDDEN_API_TOKEN
            }
        }

        configState.gptModel = gptModelField!!.selectedItem!!.toString()
        configState.newLayout = newLayoutCheckbox!!.isSelected

        temperatureField?.let { field ->
            temperatureFormatter.parse(field.text).toDouble().let {
                if (it in 0.0..2.0) {
                    configState.temperature = it
                }
            }
        }
    }

    override fun reset() {
        gptApiKeyField!!.text = HIDDEN_API_TOKEN
        gptModelField!!.selectedItem = configState.gptModel
        temperatureField!!.value = configState.temperature
        newLayoutCheckbox!!.isSelected = configState.newLayout
    }
}