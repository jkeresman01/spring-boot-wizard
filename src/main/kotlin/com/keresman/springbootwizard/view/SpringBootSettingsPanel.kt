package com.keresman.springbootwizard.view

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.keresman.springbootwizard.model.Metadata
import com.keresman.springbootwizard.model.MetadataOption
import com.keresman.springbootwizard.model.SpringInitializrSettings
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class SpringBootSettingsPanel(private val settings: SpringInitializrSettings) : JPanel(GridBagLayout()) {
    private var metadata: Metadata? = null

    private val groupIdField = JBTextField(settings.groupId, 12)
    private val artifactIdField = JBTextField(settings.artifactId, 12)
    private val packageNameField = JBTextField(settings.packageName, 12)
    private val descriptionField = JTextArea(1, 12).apply {
        text = settings.description
        lineWrap = true
        wrapStyleWord = true
    }

    private val languageCombo = JComboBox<String>()
    private val projectTypeCombo = JComboBox<String>()
    private val bootVersionCombo = JComboBox<MetadataOption>()
    private val packagingCombo = JComboBox<String>()
    private val javaVersionCombo = JComboBox<MetadataOption>()

    init {
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        initComponents()
        layoutComponents()
    }

    private fun initComponents() {
        groupIdField.document?.addDocumentListener(createDocumentListener(groupIdField) { settings.groupId = it })
        artifactIdField.document?.addDocumentListener(createDocumentListener(artifactIdField) { settings.artifactId = it })
        packageNameField.document?.addDocumentListener(createDocumentListener(packageNameField) { settings.packageName = it })
        descriptionField.document?.addDocumentListener(createDocumentListener(descriptionField) { settings.description = it })

        languageCombo.addActionListener {
            (languageCombo.selectedItem as? String)?.let { settings.language = it }
        }
        projectTypeCombo.addActionListener {
            (projectTypeCombo.selectedItem as? String)?.let { settings.projectType = it }
        }
        bootVersionCombo.addActionListener {
            (bootVersionCombo.selectedItem as? MetadataOption)?.let { settings.bootVersion = it.id }
        }
        packagingCombo.addActionListener {
            (packagingCombo.selectedItem as? String)?.let { settings.packaging = it }
        }
        javaVersionCombo.addActionListener {
            (javaVersionCombo.selectedItem as? MetadataOption)?.let { settings.javaVersion = it.id }
        }
    }

    private fun layoutComponents() {
        var row = 0

        addGroupLabel("Language & Build", row++)
        addField("Language:", languageCombo, row++)
        addField("Type:", projectTypeCombo, row++)
        addField("Spring Boot Version:", bootVersionCombo, row++)
        addField("Packaging:", packagingCombo, row++)
        addField("Java Version:", javaVersionCombo, row++)

        addGroupLabel("Project Metadata", row++)
        addField("Group:", groupIdField, row++)
        addField("Artifact:", artifactIdField, row++)
        addField("Package Name:", packageNameField, row++)
        addField("Description:", JBScrollPane(descriptionField), row++)

        // Add glue to push everything to top
        val gbc = GridBagConstraints().apply {
            weighty = 1.0
            fill = GridBagConstraints.BOTH
            gridx = 0
            gridy = row
            gridwidth = 2
        }
        add(Box.createVerticalGlue(), gbc)
    }

    private fun addGroupLabel(text: String, row: Int) {
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            JBUI.insets(10, 5, 8, 5).also { insets = it }
            gridx = 0
            gridy = row
            gridwidth = 2
        }

        val label = JBLabel(text).apply {
            font = font.deriveFont(Font.BOLD, 12f)
        }
        add(label, gbc)
    }

    private fun addField(label: String, component: JComponent, row: Int) {
        val gbc1 = GridBagConstraints().apply {
            insets = JBUI.insets(2, 5)
            gridx = 0
            gridy = row
            anchor = GridBagConstraints.NORTHWEST
        }
        add(JLabel(label).apply { font = font.deriveFont(11f) }, gbc1)

        val gbc2 = GridBagConstraints().apply {
            insets = JBUI.insets(2, 5)
            gridx = 1
            gridy = row
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }
        add(component, gbc2)
    }

    private fun addCheckBox(label: String, row: Int) {
        val gbc = GridBagConstraints().apply {
            insets = JBUI.insets(2, 5)
            gridx = 1
            gridy = row
            anchor = GridBagConstraints.WEST
        }
        add(JCheckBox(label).apply { font = font.deriveFont(11f) }, gbc)
    }

    private fun createDocumentListener(component: JTextComponent, consumer: (String) -> Unit): DocumentListener {
        return object : SimpleDocumentListener() {
            override fun update() {
                consumer(component.text)
            }
        }
    }

    fun setMetadata(metadata: Metadata) {
        this.metadata = metadata

        languageCombo.removeAllItems()
        metadata.language?.values?.forEach { languageCombo.addItem(it.name) }
        languageCombo.selectedItem = settings.language

        projectTypeCombo.removeAllItems()
        metadata.type?.values?.forEach { projectTypeCombo.addItem(it.name) }
        projectTypeCombo.selectedItem = "Gradle - Groovy"

        bootVersionCombo.removeAllItems()
        metadata.bootVersion?.values?.forEach { bootVersionCombo.addItem(it) }
        bootVersionCombo.selectedItem = metadata.bootVersion?.values
            ?.firstOrNull { it.id.contains(settings.bootVersion) }

        packagingCombo.removeAllItems()
        metadata.packaging?.values?.forEach { packagingCombo.addItem(it.name) }
        packagingCombo.selectedItem = settings.packaging

        javaVersionCombo.removeAllItems()
        metadata.javaVersion?.values?.forEach { javaVersionCombo.addItem(it) }
        javaVersionCombo.selectedItem = metadata.javaVersion?.values
            ?.firstOrNull { it.id == settings.javaVersion }
    }
}

abstract class SimpleDocumentListener : DocumentListener {
    protected abstract fun update()

    override fun insertUpdate(e: DocumentEvent) = update()
    override fun removeUpdate(e: DocumentEvent) = update()
    override fun changedUpdate(e: DocumentEvent) = update()
}