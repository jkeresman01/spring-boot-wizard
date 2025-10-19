package com.keresman.springbootwizard.view

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.keresman.springbootwizard.model.Dependency
import com.keresman.springbootwizard.model.DependenciesContainer
import com.keresman.springbootwizard.model.SpringInitializrSettings
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.*

class SpringBootDependenciesPanel(private val settings: SpringInitializrSettings) : JPanel(BorderLayout(10, 10)) {
    private var dependenciesContainer: DependenciesContainer? = null

    private val searchField = JBTextField()
    private val dependenciesPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = UIUtil.getListBackground()
    }
    private val selectedDependenciesPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = UIUtil.getListBackground()
        border = JBUI.Borders.empty(10)
    }

    private val dependencyCheckboxes = mutableMapOf<String, JBCheckBox>()
    private val categoryPanels = mutableMapOf<String, JPanel>()
    private val allDependencies = mutableMapOf<String, Dependency>()

    init {
        border = JBUI.Borders.empty(10)
        initComponents()
    }

    private fun initComponents() {
        val searchPanel = JPanel(BorderLayout(5, 3)).apply {
            border = JBUI.Borders.emptyBottom(5)
            background = UIUtil.getPanelBackground()
        }
        val depsLabel = JBLabel("Dependencies:").apply {
            font = font.deriveFont(Font.BOLD, 9f)
        }
        searchPanel.add(depsLabel, BorderLayout.NORTH)
        searchPanel.add(searchField, BorderLayout.CENTER)

        searchField.document?.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = filterDependencies(searchField.text)
            override fun removeUpdate(e: DocumentEvent) = filterDependencies(searchField.text)
            override fun changedUpdate(e: DocumentEvent) = filterDependencies(searchField.text)
        })

        val leftPanel = JPanel(BorderLayout()).apply {
            background = UIUtil.getListBackground()
            preferredSize = Dimension(400, 300)
        }
        val scrollPane = JBScrollPane(dependenciesPanel).apply {
            border = JBUI.Borders.empty()
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }
        leftPanel.add(scrollPane, BorderLayout.CENTER)

        val rightPanel = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(200, 300)
            background = UIUtil.getPanelBackground()
        }
        val selectedLabel = JBLabel("Added:").apply {
            font = font.deriveFont(Font.BOLD, 9f)
            border = JBUI.Borders.empty(5)
        }
        val selectedScrollPane = JBScrollPane(selectedDependenciesPanel).apply {
            border = JBUI.Borders.customLine(UIUtil.getBoundsColor(), 0, 1, 0, 0)
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }
        rightPanel.add(selectedLabel, BorderLayout.NORTH)
        rightPanel.add(selectedScrollPane, BorderLayout.CENTER)

        val contentSplitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel).apply {
            dividerLocation = 400
            resizeWeight = 0.65
            border = JBUI.Borders.empty()
        }

        add(searchPanel, BorderLayout.NORTH)
        add(contentSplitPane, BorderLayout.CENTER)
    }

    private fun populateDependencies() {
        dependenciesPanel.removeAll()
        dependencyCheckboxes.clear()
        categoryPanels.clear()
        allDependencies.clear()

        val container = this.dependenciesContainer ?: return
        val groups = container.values

        groups?.forEach { group ->
            val categoryPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                alignmentX = LEFT_ALIGNMENT
                background = UIUtil.getListBackground()
                border = JBUI.Borders.empty(5, 10)
            }

            val headerPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
                background = UIUtil.getListBackground()
                border = JBUI.Borders.emptyBottom(5)
            }

            val expandButton = JButton("▼").apply {
                isBorderPainted = false
                isContentAreaFilled = false
                font = Font(Font.SANS_SERIF, Font.PLAIN, 10)
                preferredSize = Dimension(16, 16)
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }

            val categoryLabel = JBLabel(group.name ?: "Dependencies").apply {
                font = font.deriveFont(Font.BOLD, 12f)
            }

            headerPanel.add(expandButton)
            headerPanel.add(Box.createHorizontalStrut(5))
            headerPanel.add(categoryLabel)

            val depsContainer = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                background = UIUtil.getListBackground()
                border = JBUI.Borders.emptyLeft(20)
            }

            group.values?.forEach { dependency ->
                val checkboxPanel = JPanel(BorderLayout()).apply {
                    background = UIUtil.getListBackground()
                    maximumSize = Dimension(Integer.MAX_VALUE, 30)
                    border = JBUI.Borders.empty(2, 0)
                }

                val checkbox = JBCheckBox(dependency.name ?: dependency.id).apply {
                    toolTipText = dependency.description
                    background = UIUtil.getListBackground()
                    addActionListener {
                        updateSelectedDependencies()
                    }
                }

                checkboxPanel.add(checkbox, BorderLayout.WEST)
                dependencyCheckboxes[dependency.id] = checkbox
                allDependencies[dependency.id] = dependency
                depsContainer.add(checkboxPanel)
            }

            expandButton.addActionListener {
                depsContainer.isVisible = !depsContainer.isVisible
                expandButton.text = if (depsContainer.isVisible) "▼" else "▶"
                dependenciesPanel.revalidate()
                dependenciesPanel.repaint()
            }

            categoryPanel.add(headerPanel)
            categoryPanel.add(depsContainer)

            categoryPanels[group.name ?: "Dependencies"] = categoryPanel
            dependenciesPanel.add(categoryPanel)
        }

        updateSelectedDependencies()
        dependenciesPanel.revalidate()
        dependenciesPanel.repaint()
    }

    private fun filterDependencies(query: String) {
        val lowerQuery = query.lowercase().trim()

        if (lowerQuery.isEmpty()) {
            categoryPanels.values.forEach { it.isVisible = true }
            dependencyCheckboxes.values.forEach { it.isVisible = true }
        } else {
            categoryPanels.forEach { (_, panel) ->
                var hasVisibleChild = false

                for (component in (panel.components.getOrNull(1) as? JPanel)?.components.orEmpty()) {
                    if (component is JPanel) {
                        val checkbox = component.components.getOrNull(0) as? JBCheckBox
                        if (checkbox != null) {
                            val visible = checkbox.text.lowercase().contains(lowerQuery) ||
                                    checkbox.toolTipText?.lowercase()?.contains(lowerQuery) == true
                            component.isVisible = visible
                            if (visible) hasVisibleChild = true
                        }
                    }
                }

                panel.isVisible = hasVisibleChild
            }
        }

        dependenciesPanel.revalidate()
        dependenciesPanel.repaint()
    }

    private fun updateSelectedDependencies() {
        selectedDependenciesPanel.removeAll()

        val selected = dependencyCheckboxes.filter { it.value.isSelected }

        if (selected.isEmpty()) {
            val emptyLabel = JBLabel("<html><center>No dependencies added</center></html>").apply {
                foreground = UIUtil.getInactiveTextColor()
                horizontalAlignment = SwingConstants.CENTER
                border = JBUI.Borders.empty(20)
            }
            selectedDependenciesPanel.add(emptyLabel)
        } else {
            selected.forEach { (id, _) ->
                val dependency = allDependencies[id]
                if (dependency != null) {
                    val depPanel = JPanel(BorderLayout()).apply {
                        background = UIUtil.getListBackground()
                        maximumSize = Dimension(Integer.MAX_VALUE, 40)
                        border = JBUI.Borders.empty(5, 5, 5, 5)
                    }

                    val nameLabel = JBLabel(dependency.name ?: dependency.id).apply {
                        font = font.deriveFont(Font.BOLD, 11f)
                    }

                    val descLabel = JBLabel("<html><font color='gray'>${dependency.description ?: ""}</font></html>").apply {
                        font = font.deriveFont(10f)
                    }

                    val textPanel = JPanel().apply {
                        layout = BoxLayout(this, BoxLayout.Y_AXIS)
                        background = UIUtil.getListBackground()
                        add(nameLabel)
                        add(descLabel)
                    }

                    depPanel.add(textPanel, BorderLayout.CENTER)
                    selectedDependenciesPanel.add(depPanel)
                }
            }
        }

        settings.clearDependencies()
        dependencyCheckboxes.filter { it.value.isSelected }.forEach { (id, _) ->
            allDependencies[id]?.let {
                settings.addDependency(it.id)
            }
        }

        selectedDependenciesPanel.revalidate()
        selectedDependenciesPanel.repaint()
    }

    fun setDependencies(dependenciesContainer: DependenciesContainer) {
        this.dependenciesContainer = dependenciesContainer
        populateDependencies()
    }
}