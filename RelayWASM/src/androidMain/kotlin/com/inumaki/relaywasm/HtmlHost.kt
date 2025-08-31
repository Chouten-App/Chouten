package com.inumaki.relaywasm

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import java.util.concurrent.atomic.AtomicInteger

object HtmlHost {
    private val nextId = AtomicInteger(1)
    private val documents = mutableMapOf<Int, Document>()

    fun parse(html: String): Int {
        val doc = Ksoup.parse(html)
        val id = nextId.getAndIncrement()
        documents.put(id, doc)
        return id
    }

    fun querySelector(docId: Int, selector: String): Int {
        val doc = documents[docId] ?: return 0
        val element = doc.selectFirst(selector) ?: return 0
        // Store element internally (or return a wrapper ID)
        val elementId = nextId.getAndIncrement()
        elements[elementId] = element
        return elementId
    }

    fun querySelectorAll(docId: Int, selector: String): List<Int> {
        val doc = documents[docId] ?: return emptyList()
        val matchedElements = doc.select(selector)

        return matchedElements.map { element ->
            val elementId = nextId.getAndIncrement()
            elements[elementId] = element
            elementId
        }
    }

    fun nodeQuerySelector(nodeId: Int, selector: String): Int {
        val node = elements[nodeId] ?: return 0
        val element = node.selectFirst(selector) ?: return 0
        val elementId = nextId.getAndIncrement()
        elements[elementId] = element
        return elementId
    }

    fun nodeQuerySelectorAll(nodeId: Int, selector: String): List<Int> {
        val node = elements[nodeId] ?: return emptyList()
        val selected = node.select(selector)
        return selected.map { element ->
            val id = nextId.getAndIncrement()
            elements[id] = element
            id
        }
    }

    fun nodeText(elementId: Int): String {
        val element = elements[elementId] ?: return "Fail"
        return element.text()
    }

    fun nodeAttr(elementId: Int, attr: String): String {
        val element = elements[elementId] ?: return "Fail"
        return element.attr(attr)
    }

    fun freeDocument(docId: Int) {
        documents.remove(docId)
    }

    // Optional: store elements separately
    private val elements = mutableMapOf<Int, Element>()
}