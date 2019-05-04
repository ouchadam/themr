package com.github.ouchadam.themr

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceSet
import com.squareup.javapoet.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.w3c.dom.Element
import java.io.File
import java.lang.IllegalStateException
import javax.xml.parsers.DocumentBuilderFactory

class ThemrPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("themr", ThemrPluginExtension::class.java)

    project.plugins.withType(AppPlugin::class.java) { plugin ->
      plugin.extension.sourceSets { registerGeneratedSources(it, project) }
    }

    project.plugins.withType(LibraryPlugin::class.java) { plugin ->
      plugin.extension.sourceSets { registerGeneratedSources(it, project) }
    }

    project.task("themrGenerateThemes") {
      it.doLast {
        val styles = parseResourceStyles(extension, project)
        val themrStyles = createThemeCombinations(styles, extension.combinations)

        writeGeneratedStyles(project, createOutputStyles(themrStyles))
        writeGeneratedSource(project, createThemR(readPackageName(project), themrStyles))
      }
    }
    project.afterEvaluate {
      project.tasks.getByName("preBuild").dependsOn("themrGenerateThemes")
    }
  }

  private fun registerGeneratedSources(sourceSets: NamedDomainObjectContainer<AndroidSourceSet>, project: Project) {
    val main = sourceSets.getByName("main")
    main.res.srcDirs(project.file("build/generated/res/themr"))
    main.java.srcDirs(project.file("build/generated/source/themr"))
  }

  private fun writeGeneratedStyles(project: Project, stylesFileContents: String) {
    val directory = project.file("build/generated/res/themr/values")
    if (!directory.exists()) directory.mkdirs()
    File(directory, "gen-themr.xml").writeText(stylesFileContents)
  }

  private fun writeGeneratedSource(project: Project, javaFile: JavaFile) {
    val directory = project.file("build/generated/source/themr")
    if (!directory.exists()) directory.mkdirs()
    javaFile.writeTo(directory)
  }

  private fun createThemeCombinations(styles: Map<String, Style>, combinations: Map<String, List<String>>): List<ThemrStyle> {
    return combinations.entries.map {
      val themeStyle = styles.getValue(it.key)
      it.value.map { paletteName ->
        val paletteStyle = styles.getValue(paletteName)
        ThemrStyle(
            Style(
                name = "${paletteStyle.name}_${themeStyle.name}",
                items = paletteStyle.items.plus(themeStyle.items),
                parent = themeStyle.parent
            ),
            palette = paletteStyle.name,
            theme = themeStyle.name
        )
      }
    }.flatten()
  }

  private fun readPackageName(project: Project): String {
    return project.plugins.findPlugin(AppPlugin::class.java)?.extension?.defaultConfig?.applicationId ?: project.plugins.findPlugin(
        LibraryPlugin::class.java)?.extension?.defaultConfig?.applicationId ?: throw IllegalStateException("The project doesn't apply an android plugin!")
  }
}

internal data class ThemrStyle(val style: Style, val palette: String, val theme: String)

open class ThemrPluginExtension {
  var source: List<String> = listOf("themr")
  var combinations = emptyMap<String, List<String>>()
}
