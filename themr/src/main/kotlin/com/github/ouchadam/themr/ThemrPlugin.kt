package com.github.ouchadam.themr

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class ThemrPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("themr", ThemrPluginExtension::class.java)

    project.plugins.withType(AppPlugin::class.java) { plugin ->
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
            content = paletteStyle.content.plus(themeStyle.content),
            parent = themeStyle.parent
        )
      }
    }.flatten()
  }

  private fun createOutputStyles(outputStyles: List<Style>): String {
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<resources>\n" + outputStyles.map {
      "<style name=\"${it.name}\" ${it.parent?.let { "parent=\"$it\"" }}>\n${it.content}\n</style>"
    }.joinToString("\n") + "\n</resources>"
  }

  private fun readThemeStyles(file: File): Map<String, Style> {
    val inputAsString = file.readText()
    val toRegex = "<style(.|\\n)+?</style>(.|\\n)".toRegex(RegexOption.DOT_MATCHES_ALL)
    return toRegex.findAll(inputAsString).map { toStyle(it.value) }
        .associate { it.name to it }
  }

  private fun toStyle(rawStyle: String): Style {
    val styleName = "style name=\"(.+?)\"".toRegex().find(rawStyle)!!.groupValues[1].trim()
    val parentName = "parent=\"(.+)\"".toRegex().find(rawStyle)?.groupValues?.get(1)?.trim()
    val content = "<style .+?>((.|\\n)+?)</style>(.|\\n)".toRegex().find(rawStyle)!!.groupValues[1].trim()
    return Style(styleName, content, parentName)
  }

}

internal data class Style(val name: String, val content: String, val parent: String?)

open class ThemrPluginExtension {
  var source: String = "themr"
  var combinations = emptyMap<String, List<String>>()
}