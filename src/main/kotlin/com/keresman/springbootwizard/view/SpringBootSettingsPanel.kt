package com.keresman.springbootwizard.view

import com.intellij.ui.components.*
import com.intellij.util.ui.JBUI
import com.keresman.springbootwizard.listeners.SimpleDocumentListener
import com.keresman.springbootwizard.model.*
import java.awt.*
import javax.swing.*
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

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
        initListeners()
        layoutComponents()
    }

    fun setMetadata(metadata: Metadata) {
        this.metadata = metadata
        populateCombos(metadata)
    }

    private fun initListeners() {
        addFieldListeners()
        addComboListeners()
    }

    private fun addFieldListeners() {
        groupIdField.addListener { settings.groupId = it }
        artifactIdField.addListener { settings.artifactId = it }
        packageNameField.addListener { settings.packageName = it }
        descriptionField.addListener { settings.description = it }
    }

    private fun addComboListeners() {
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

        addGlue(row)
    }

    private fun addGroupLabel(text: String, row: Int) {
        val gbc = constraints(row, 0, 2).apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(10, 5, 8, 5)
        }
        val label = JBLabel(text).apply { font = font.deriveFont(Font.BOLD, 12f) }
        add(label, gbc)
    }

    private fun addField(label: String, component: JComponent, row: Int) {
        val labelConstraints = constraints(row, 0).apply {
            anchor = GridBagConstraints.NORTHWEST
            insets = JBUI.insets(2, 5)
        }
        val fieldConstraints = constraints(row, 1).apply {
            insets = JBUI.insets(2, 5)
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }

        add(JLabel(label).apply { font = font.deriveFont(11f) }, labelConstraints)
        add(component, fieldConstraints)
    }

    private fun addGlue(row: Int) {
        val gbc = constraints(row, 0, 2).apply {
            weighty = 1.0
            fill = GridBagConstraints.BOTH
        }
        add(Box.createVerticalGlue(), gbc)
    }

    private fun populateCombos(metadata: Metadata) {
        populateCombo(languageCombo, metadata.language?.values?.map { it.name }, settings.language)
        populateCombo(projectTypeCombo, metadata.type?.values?.map { it.name }, "Gradle - Groovy")

        populateCombo(bootVersionCombo, metadata.bootVersion?.values, metadata.bootVersion?.values
            ?.firstOrNull { it.id.contains(settings.bootVersion) })

        populateCombo(packagingCombo, metadata.packaging?.values?.map { it.name }, settings.packaging)

        populateCombo(javaVersionCombo, metadata.javaVersion?.values, metadata.javaVersion?.values
            ?.firstOrNull { it.id == settings.javaVersion })
    }

    private fun <T> populateCombo(combo: JComboBox<T>, items: List<T>?, selected: Any?) {
        combo.removeAllItems()
        items?.forEach { combo.addItem(it) }
        combo.selectedItem = selected
    }

    private fun constraints(row: Int, col: Int, width: Int = 1): GridBagConstraints {
        return GridBagConstraints().apply {
            gridx = col
            gridy = row
            gridwidth = width
        }
    }

    private fun JTextComponent.addListener(consumer: (String) -> Unit) {
        document?.addDocumentListener(createDocumentListener(this, consumer))
    }

    private fun createDocumentListener(component: JTextComponent, consumer: (String) -> Unit): DocumentListener {
        return object : SimpleDocumentListener() {
            override fun update() {
                consumer(component.text)
            }
        }
    }
}
