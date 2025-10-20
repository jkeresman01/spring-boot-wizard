package com.keresman.springbootwizard.view

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.keresman.springbootwizard.model.Metadata
import com.keresman.springbootwizard.model.SpringInitializrSettings
import com.keresman.springbootwizard.service.MetadataService
import java.awt.*
import javax.swing.*

class SpringBootWizardDialog : DialogWrapper(true) {

    private val settings = SpringInitializrSettings()
    private val metadataService = MetadataService()
    private var metadata: Metadata? = null

    private lateinit var tabbedPane: JTabbedPane
    private lateinit var settingsPanel: SpringBootSettingsPanel
    private lateinit var dependenciesPanel: SpringBootDependenciesPanel

    init {
        title = "Spring Boot Project Wizard"
        isResizable = true
        init()
        loadMetadataAsync()
    }

    fun getSettings() = settings
    fun getMetadataService() = metadataService
    fun getMetadata() = metadata

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(550, 480)
        }

        tabbedPane = JTabbedPane().apply {
            settingsPanel = SpringBootSettingsPanel(settings)
            addTab("Configuration", settingsPanel)

            dependenciesPanel = SpringBootDependenciesPanel(settings)
            addTab("Dependencies", dependenciesPanel)
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER)
        return mainPanel
    }

    override fun doOKAction() {
        if (!isFormValid()) return
        super.doOKAction()
    }

    private fun loadMetadataAsync() {
        Thread {
            try {
                val fetchedMetadata = metadataService.fetchMetadataSync()
                SwingUtilities.invokeLater { applyMetadata(fetchedMetadata) }
            } catch (e: Exception) {
                SwingUtilities.invokeLater { showMetadataError(e) }
            }
        }.start()
    }

    private fun applyMetadata(metadata: Metadata?) {
        if (metadata == null) return

        this.metadata = metadata
        settingsPanel.setMetadata(metadata)
        metadata.dependencies?.let { dependenciesPanel.setDependencies(it) }
    }

    private fun showMetadataError(e: Exception) {
        Messages.showErrorDialog(
            "Failed to load metadata: ${e.message}",
            "Error"
        )
    }

    private fun isFormValid(): Boolean {
        return when {
            settings.projectName.isBlank() -> showValidationError("Project name cannot be empty")
            settings.groupId.isBlank() || settings.artifactId.isBlank() ->
                showValidationError("Group ID and Artifact ID must be set")
            settings.packageName.isBlank() ->
                showValidationError("Package name must be set")
            else -> true
        }
    }

    private fun showValidationError(message: String): Boolean {
        Messages.showErrorDialog(message, "Validation Error")
        return false
    }
}
