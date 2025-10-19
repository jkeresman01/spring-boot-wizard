package com.keresman.springbootwizard

import com.intellij.facet.ui.ValidationResult
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectGenerator
import com.keresman.springbootwizard.model.SpringInitializrSettings
import com.keresman.springbootwizard.service.MetadataService
import com.keresman.springbootwizard.view.SpringBootWizardDialog
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream
import javax.swing.Icon

class SpringBootProjectGenerator : DirectoryProjectGenerator<SpringInitializrSettings> {
    private var settings: SpringInitializrSettings? = null
    private var metadataService: MetadataService? = null
    private var dialog: SpringBootWizardDialog? = null

    override fun getName():String = "SPRING BOOT"

    override fun getLogo(): Icon? = null

    override fun validate(baseDirPath: String): ValidationResult {
        return ValidationResult.OK
    }

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: SpringInitializrSettings,
        module: Module
    ) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Creating Spring Boot Project",
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    indicator.text = "Downloading Spring Boot project..."
                    generateSpringBootProject(baseDir, settings)
                    indicator.text = "Project created successfully"
                } catch (e: Exception) {
                    throw RuntimeException("Failed to create Spring Boot project: ${e.message}", e)
                }
            }
        })
    }

    fun showProjectSettings(context: WizardContext?): SpringInitializrSettings? {
        dialog = SpringBootWizardDialog()

        //------------------------------------
        // Two times stupid !!
        //------------------------------------
        return if (dialog!!.showAndGet()) {
            settings = dialog!!.getSettings()
            metadataService = dialog!!.getMetadataService()
            settings
        } else {
            null
        }
    }

    private fun generateSpringBootProject(baseDir: VirtualFile, settings: SpringInitializrSettings) {
        val metadataService = metadataService ?: throw RuntimeException("MetadataService not initialized")

        val downloadUrl = buildDownloadUrl(settings)
        val zipData = metadataService.downloadProjectZip(downloadUrl)
        extractZip(zipData, baseDir)

        baseDir.refresh(false, true)
    }

    private fun buildDownloadUrl(settings: SpringInitializrSettings): String {
        val metadataService = metadataService ?: throw RuntimeException("MetadataService not initialized")

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

    private fun extractZip(zipData: ByteArray, targetDir: VirtualFile) {
        val basePath = File(targetDir.path)
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