package com.keresman.springbootwizard.view

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.keresman.springbootwizard.model.Dependency
import com.keresman.springbootwizard.model.DependencyContainer
import com.keresman.springbootwizard.model.DependencyGroup
import com.keresman.springbootwizard.model.SpringInitializrSettings
import java.awt.*
import javax.swing.*
import javax.swing.border.Border
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SpringBootDependenciesPanel(private val settings: SpringInitializrSettings) : JPanel(BorderLayout(10, 10)) {

    private var dependencyContainer: DependencyContainer? = null

    private val searchField = JBTextField()
    private val dependenciesPanel = createVerticalPanel()
    private val selectedDependenciesPanel = createVerticalPanel(JBUI.Borders.empty(10))

    private val dependencyCheckboxes = mutableMapOf<String, JBCheckBox>()
    private val categoryPanels = mutableMapOf<String, JPanel>()
    private val allDependencies = mutableMapOf<String, Dependency>()

    init {
        border = JBUI.Borders.empty(10)
        initComponents()
    }

    fun setDependencies(dependencyContainer: DependencyContainer) {
        this.dependencyContainer = dependencyContainer
        populateDependencies()
    }

    private fun initComponents() {
        val searchPanel = createSearchPanel()
        val leftPanel = createLeftPanel()
        val rightPanel = createRightPanel()
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel).apply {
            dividerLocation = 400
            resizeWeight = 0.65
            border = JBUI.Borders.empty()
        }
        add(searchPanel, BorderLayout.NORTH)
        add(splitPane, BorderLayout.CENTER)
    }

    private fun createSearchPanel(): JPanel {
        val panel = JPanel(BorderLayout(5, 3)).apply {
            border = JBUI.Borders.emptyBottom(5)
            background = UIUtil.getPanelBackground()
        }
        val label = JBLabel("Dependencies:").apply {
            font = font.deriveFont(Font.BOLD, 9f)
        }
        panel.add(label, BorderLayout.NORTH)
        panel.add(searchField, BorderLayout.CENTER)
        addSearchListener()
        return panel
    }

    private fun addSearchListener() {
        searchField.document?.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = filterDependencies(searchField.text)
            override fun removeUpdate(e: DocumentEvent) = filterDependencies(searchField.text)
            override fun changedUpdate(e: DocumentEvent) = filterDependencies(searchField.text)
        })
    }

    private fun createLeftPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = UIUtil.getListBackground()
            preferredSize = Dimension(400, 300)
        }
        val scroll = JBScrollPane(dependenciesPanel).apply {
            border = JBUI.Borders.empty()
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }
        panel.add(scroll, BorderLayout.CENTER)
        return panel
    }

    private fun createRightPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(200, 300)
            background = UIUtil.getPanelBackground()
        }
        val label = JBLabel("Added:").apply {
            font = font.deriveFont(Font.BOLD, 9f)
            border = JBUI.Borders.empty(5)
        }
        val scroll = JBScrollPane(selectedDependenciesPanel).apply {
            border = JBUI.Borders.customLine(UIUtil.getBoundsColor(), 0, 1, 0, 0)
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }
        panel.add(label, BorderLayout.NORTH)
        panel.add(scroll, BorderLayout.CENTER)
        return panel
    }

    private fun populateDependencies() {
        clearDependencies()
        dependencyContainer?.values?.forEach { group ->
            val categoryPanel = createCategoryPanel(group)
            categoryPanels[group.name ?: "Dependencies"] = categoryPanel
            dependenciesPanel.add(categoryPanel)
        }
        updateSelectedDependencies()
        dependenciesPanel.revalidate()
        dependenciesPanel.repaint()
    }

    private fun clearDependencies() {
        dependenciesPanel.removeAll()
        dependencyCheckboxes.clear()
        categoryPanels.clear()
        allDependencies.clear()
    }

    private fun createCategoryPanel(group: DependencyGroup): JPanel {
        val categoryPanel = createVerticalPanel(JBUI.Borders.empty(5, 10))
        val (header, expandButton) = createHeaderPanel(group.name ?: "Dependencies")
        val depsContainer = createDependenciesContainer(group)
        categoryPanel.add(header)
        categoryPanel.add(depsContainer)
        expandButton.addActionListener {
            depsContainer.isVisible = !depsContainer.isVisible
            expandButton.text = if (depsContainer.isVisible) "▼" else "▶"
            dependenciesPanel.revalidate()
            dependenciesPanel.repaint()
        }
        return categoryPanel
    }

    private fun createHeaderPanel(name: String): Pair<JPanel, JButton> {
        val expandButton = JButton("▼").apply {
            isBorderPainted = false
            isContentAreaFilled = false
            font = Font(Font.SANS_SERIF, Font.PLAIN, 10)
            preferredSize = Dimension(16, 16)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        }
        val label = JBLabel(name).apply {
            font = font.deriveFont(Font.BOLD, 12f)
        }
        val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            background = UIUtil.getListBackground()
            border = JBUI.Borders.emptyBottom(5)
            add(expandButton)
            add(Box.createHorizontalStrut(5))
            add(label)
        }
        return panel to expandButton
    }

    private fun createDependenciesContainer(group: DependencyGroup): JPanel {
        val container = createVerticalPanel(JBUI.Borders.emptyLeft(20))
        group.values?.forEach { dependency ->
            container.add(createDependencyRow(dependency))
        }
        return container
    }

    private fun createDependencyRow(dependency: Dependency): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = UIUtil.getListBackground()
            maximumSize = Dimension(Int.MAX_VALUE, 30)
            border = JBUI.Borders.empty(2, 0)
        }
        val checkbox = JBCheckBox(dependency.name ?: dependency.id).apply {
            toolTipText = dependency.description
            background = UIUtil.getListBackground()
            addActionListener { updateSelectedDependencies() }
        }
        dependencyCheckboxes[dependency.id] = checkbox
        allDependencies[dependency.id] = dependency
        panel.add(checkbox, BorderLayout.WEST)
        return panel
    }

    private fun filterDependencies(query: String) {
        val lowerQuery = query.lowercase().trim()
        if (lowerQuery.isEmpty()) showAllDependencies() else filterByQuery(lowerQuery)
        dependenciesPanel.revalidate()
        dependenciesPanel.repaint()
    }

    private fun showAllDependencies() {
        categoryPanels.values.forEach { it.isVisible = true }
        dependencyCheckboxes.values.forEach { it.isVisible = true }
    }

    private fun filterByQuery(query: String) {
        categoryPanels.forEach { (_, panel) ->
            var hasVisibleChild = false
            val depsPanel = panel.components.getOrNull(1) as? JPanel ?: return@forEach
            for (component in depsPanel.components) {
                if (component is JPanel) {
                    val checkbox = component.components.getOrNull(0) as? JBCheckBox
                    val visible = checkbox?.let {
                        it.text.lowercase().contains(query) || it.toolTipText?.lowercase()?.contains(query) == true
                    } ?: false
                    component.isVisible = visible
                    if (visible) hasVisibleChild = true
                }
            }
            panel.isVisible = hasVisibleChild
        }
    }

    private fun updateSelectedDependencies() {
        selectedDependenciesPanel.removeAll()
        val selected = dependencyCheckboxes.filter { it.value.isSelected }
        if (selected.isEmpty()) addEmptyLabel() else addSelectedItems(selected)
        updateSettings(selected)
        selectedDependenciesPanel.revalidate()
        selectedDependenciesPanel.repaint()
    }

    private fun addEmptyLabel() {
        val label = JBLabel("<html><center>No dependencies added</center></html>").apply {
            foreground = UIUtil.getInactiveTextColor()
            horizontalAlignment = SwingConstants.CENTER
            border = JBUI.Borders.empty(20)
        }
        selectedDependenciesPanel.add(label)
    }

    private fun addSelectedItems(selected: Map<String, JBCheckBox>) {
        selected.forEach { (id, _) ->
            val dependency = allDependencies[id] ?: return@forEach
            selectedDependenciesPanel.add(createSelectedDependencyPanel(dependency))
        }
    }

    private fun createSelectedDependencyPanel(dependency: Dependency): JPanel {
        val nameLabel = JBLabel(dependency.name ?: dependency.id).apply {
            font = font.deriveFont(Font.BOLD, 11f)
        }
        val descLabel = JBLabel("<html><font color='gray'>${dependency.description ?: ""}</font></html>").apply {
            font = font.deriveFont(10f)
        }
        val textPanel = createVerticalPanel().apply {
            add(nameLabel)
            add(descLabel)
        }
        return JPanel(BorderLayout()).apply {
            background = UIUtil.getListBackground()
            maximumSize = Dimension(Int.MAX_VALUE, 40)
            border = JBUI.Borders.empty(5)
            add(textPanel, BorderLayout.CENTER)
        }
    }

    private fun updateSettings(selected: Map<String, JBCheckBox>) {
        settings.clearDependencies()
        selected.keys.forEach { id ->
            allDependencies[id]?.let { settings.addDependency(it.id) }
        }
    }

    private fun createVerticalPanel(border: Border? = null): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = UIUtil.getListBackground()
            if (border != null) this.border = border
        }
    }
}
