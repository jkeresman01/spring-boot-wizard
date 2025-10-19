package com.keresman.springbootwizard.model
class SpringInitializrSettings {

    var serverUrl: String = "https://start.spring.io"
    var projectName: String = "demo"
    var projectPath: String? = null

    var projectType: String = "gradle-project"
    var language: String = "Java"
    var bootVersion: String = "3.3.3"
    var packaging: String = "jar"
    var javaVersion: String = "21"

    var groupId: String = "com.example"
    var artifactId: String = "demo"
    var packageName: String = "com.example.demo"
    var description: String = "Demo project for Spring Boot"

    private val selectedDependencyIds = mutableSetOf<String>()

    fun addDependency(depId: String) {
        selectedDependencyIds.add(depId)
    }

    fun removeDependency(depId: String) {
        selectedDependencyIds.remove(depId)
    }

    fun getSelectedDependencyIds(): Set<String> = selectedDependencyIds.toSet()

    fun clearDependencies() {
        selectedDependencyIds.clear()
    }

    fun getDependenciesAsString(): String = selectedDependencyIds.joinToString(",")
}