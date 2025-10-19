package com.keresman.springbootwizard

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ModifiableRootModel
import com.keresman.springbootwizard.model.SpringInitializrSettings
import com.keresman.springbootwizard.service.MetadataService
import com.keresman.springbootwizard.view.SpringBootWizardDialog
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream
import javax.swing.Icon

class SpringBootModuleBuilder : ModuleBuilder() {
    private var settings: SpringInitializrSettings? = null
    private var metadataService: MetadataService? = null
    private var wizardDialog: SpringBootWizardDialog? = null

    override fun getModuleType(): ModuleType<SpringBootModuleBuilder> = StdModuleTypes.JAVA as ModuleType<SpringBootModuleBuilder>

    override fun getName() = "Spring Boot Module"

    override fun getDescription() = "Create a new Spring Boot module"

    override fun getParentGroup() = "Spring Boot"

    override fun getWeight() = 70

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        super.setupRootModel(modifiableRootModel)

        settings?.let {
            generateSpringBootProject()
        }
    }

    override fun isSuitableSdkType(sdkType: SdkTypeId?): Boolean = true

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep? {
        wizardDialog = SpringBootWizardDialog()
        return object : ModuleWizardStep() {
            override fun getComponent(): javax.swing.JComponent {
                return (wizardDialog?.contentPane as? javax.swing.JComponent) ?: javax.swing.JPanel()
            }

            override fun updateDataModel() {
                settings = wizardDialog?.getSettings()
                metadataService = wizardDialog?.getMetadataService()
            }
        }
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        return super.modifySettingsStep(settingsStep)
    }

    override fun getNodeIcon(): Icon? = null

    private fun generateSpringBootProject() {
        try {
            val downloadUrl = buildDownloadUrl()
            val metadataService = metadataService ?: return
            val zipData = metadataService.downloadProjectZip(downloadUrl)
            extractZip(zipData)
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate Spring Boot project: ${e.message}", e)
        }
    }

    private fun buildDownloadUrl(): String {
        val settings = settings ?: return ""
        val metadataService = metadataService ?: return ""

        val params = mapOf(
            "type" to mapProjectType(settings.projectType),
            "language" to mapLanguage(settings.language),
            "bootVersion" to settings.bootVersion,
            "baseDir" to settings.projectName,
            "groupId" to settings.groupId,
            "artifactId" to settings.artifactId,
            "name" to settings.projectName,
            "description" to settings.description,
            "packageName" to settings.packageName,
            "packaging" to settings.packaging,
            "javaVersion" to settings.javaVersion,
            "dependencies" to settings.getDependenciesAsString()
        ).filterValues { it.isNotEmpty() }

        return metadataService.buildDownloadUrl(settings.serverUrl, params)
    }

    private fun mapProjectType(displayType: String) = when (displayType) {
        "Gradle - Groovy" -> "gradle-project"
        "Gradle - Kotlin" -> "gradle-project-kotlin"
        "Maven" -> "maven-project"
        else -> "gradle-project"
    }

    private fun mapLanguage(displayLang: String) = displayLang.lowercase()

    private fun extractZip(zipData: ByteArray) {
        val basePath = File(contentEntryPath)
        basePath.mkdirs()

        ZipInputStream(ByteArrayInputStream(zipData)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val entryFile = File(basePath, entry.name)

                if (entry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    entryFile.parentFile?.mkdirs()
                    entryFile.outputStream().use { fos ->
                        zis.copyTo(fos)
                    }
                }

                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}