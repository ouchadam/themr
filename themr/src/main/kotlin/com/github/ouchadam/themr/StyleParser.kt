package com.github.ouchadam.themr

import org.gradle.api.file.FileCollection
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

internal fun parseResourceStyles(source: FileCollection) = source.map { file ->
  readThemeStyles(file)
}.foldRight(mutableMapOf<String, Style>()) { current, acc ->
  acc.putAll(current); acc
}

private fun readThemeStyles(file: File): Map<String, Style> {
  val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
  xmlDoc.documentElement.normalize()
  val styles = xmlDoc.getElementsByTagName("style")
  return styles?.let {
    val stylesMap = mutableMapOf<String, Style>()
    for (i in 0 until it.length) {
      val style = it.item(i)
      val name = style.attributes.getNamedItem("name").nodeValue
      val parent = style.attributes.getNamedItem("parent")?.nodeValue
      val items = (style as Element).getElementsByTagName("item")?.let { itemNodes ->
        val items = mutableListOf<Item>()
        for (itemIndex in 0 until itemNodes.length) {
          val itemNode = itemNodes.item(itemIndex)
          val itemName = itemNode.attributes.getNamedItem("name")
          items.add(Item(itemName.nodeValue, itemNode.textContent))
        }
        items
      } ?: emptyList<Item>()
      stylesMap[name] = Style(name, items, parent)
    }
    stylesMap
  } ?: emptyMap()
}

internal data class Style(val name: String, val items: List<Item>, val parent: String?)
internal data class Item(val name: String, val value: String)
