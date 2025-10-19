package com.keresman.springbootwizard.view

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.keresman.springbootwizard.model.Metadata
import com.keresman.springbootwizard.model.SpringInitializrSettings
import com.keresman.springbootwizard.service.MetadataService
import javax.swing.*
import java.awt.BorderLayout
import java.awt.Dimension

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
        loadMetadata()
    }

    private fun loadMetadata() {
        Thread {
            try {
                metadata = metadataService.fetchMetadataSync()
                SwingUtilities.invokeLater {
                    metadata?.let {
                        settingsPanel.setMetadata(it)
                        it.dependencies?.let { deps ->
                            dependenciesPanel.setDependencies(deps)
                        }
                    }
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    Messages.showErrorDialog(
                        "Failed to load metadata: ${e.message}",
                        "Error"
                    )
                }
            }
        }.start()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(550, 480)
        }

        tabbedPane = JTabbedPane()

        settingsPanel = SpringBootSettingsPanel(settings)
        tabbedPane.addTab("Configuration", settingsPanel)

        dependenciesPanel = SpringBootDependenciesPanel(settings)
        tabbedPane.addTab("Dependencies", dependenciesPanel)

        mainPanel.add(tabbedPane, BorderLayout.CENTER)
        return mainPanel
    }

    override fun doOKAction() {
        when {
            settings.projectName.isEmpty() -> {
                Messages.showErrorDialog(
                    "Project name cannot be empty",
                    "Validation Error"
                )
                return
            }
            settings.groupId.isEmpty() || settings.artifactId.isEmpty() -> {
                Messages.showErrorDialog(
                    "Group ID and Artifact ID must be set",
                    "Validation Error"
                )
                return
            }
            settings.packageName.isEmpty() -> {
                Messages.showErrorDialog(
                    "Package name must be set",
                    "Validation Error"
                )
                return
            }
        }

        super.doOKAction()
    }

    fun getSettings() = settings
    fun getMetadataService() = metadataService
    fun getMetadata() = metadata
}