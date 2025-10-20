package com.keresman.springbootwizard.listeners

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

abstract class SimpleDocumentListener : DocumentListener {
    protected abstract fun update()

    override fun insertUpdate(e: DocumentEvent) = update()
    override fun removeUpdate(e: DocumentEvent) = update()
    override fun changedUpdate(e: DocumentEvent) = update()
}
