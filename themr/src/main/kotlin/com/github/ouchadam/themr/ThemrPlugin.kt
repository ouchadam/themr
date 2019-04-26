package com.github.ouchadam.themr

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ThemrPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("themr", ThemrPluginExtension::class.java)

    project.plugins.withType(AppPlugin::class.java) { plugin ->
      plugin.extension.sourceSets {
        it.getByName("main").res.srcDirs(project.file("build/generated/res/themr"))
      }
    }

    project.plugins.withType(LibraryPlugin::class.java) { plugin ->
      plugin.extension.sourceSets {
        it.getByName("main").res.srcDirs(project.file("build/generated/res/themr"))
      }
    }

    project.task("themrGenerateThemes") {
      it.doLast {
        val styles: Map<String, Style> = readThemeStyles(project.file("src/main/res/values/${extension.source}.xml"))
        val output = createThemeCombinations(styles, extension.combinations)
        writeGeneratedStyles(project, createOutputStyles(output))
      }
    }
    project.afterEvaluate {
      project.tasks.getByName("preBuild").dependsOn("themrGenerateThemes")
    }
  }

  private fun writeGeneratedStyles(project: Project, stylesFileContents: String) {
    val directory = project.file("build/generated/res/themr/values")
    if (!directory.exists()) directory.mkdirs()
    File(directory, "gen-themr.xml").writeText(stylesFileContents)
  }

  private fun createThemeCombinations(styles: Map<String, Style>, combinations: Map<String, List<String>>): List<Style> {
    return combinations.entries.map {
      val themeStyle = styles.getValue(it.key)
      it.value.map { paletteName ->
        val paletteStyle = styles.getValue(paletteName)
        Style(
            name = "${paletteStyle.name}_${themeStyle.name}",
            items = paletteStyle.items.plus(themeStyle.items),
            parent = themeStyle.parent
        )
      }
    }.flatten()
  }

  private fun createOutputStyles(outputStyles: List<Style>): String {
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<resources>\n" + outputStyles.map {
      "<style name=\"${it.name}\" ${it.parent?.let { "parent=\"$it\"" }}>\n\t${it.items.joinToString("\n\t") {
        "<item name=\"${it.name}\">${it.value}</item>"
      }}\n</style>"
    }.joinToString("\n") + "\n</resources>"
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
}

internal data class Style(val name: String, val items: List<Item>, val parent: String?)
internal data class Item(val name: String, val value: String)

open class ThemrPluginExtension {
  var source: String = "themr"
  var combinations = emptyMap<String, List<String>>()
}