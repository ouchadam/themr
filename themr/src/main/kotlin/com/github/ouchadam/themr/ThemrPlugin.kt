package com.github.ouchadam.themr

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceSet
import com.squareup.javapoet.*
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import java.io.File
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

private const val SOURCE_GENERATED_OUTPUT_DIR = "build/generated/source/themr"
private const val RES_GENERATED_OUTPUT_DIR = "build/generated/res/themr"

class ThemrPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create("themr", ThemrPluginExtension::class.java)

    project.plugins.withType(AppPlugin::class.java) { plugin ->
      plugin.extension.sourceSets { registerGeneratedSources(it, project) }
    }

    project.plugins.withType(LibraryPlugin::class.java) { plugin ->
      plugin.extension.sourceSets { registerGeneratedSources(it, project) }
    }

    project.tasks.register("themrGenerateThemes", GenerateTask::class.java) { instance ->
      instance.paletteFiles.setFrom(project.files(extension.source.map { fileName ->
        "src/main/res/values/$fileName.xml"
      }))
      instance.combinations.set(extension.combinations)
    }

    project.afterEvaluate { project.tasks.getByName("preBuild").dependsOn("themrGenerateThemes") }
  }

  private fun registerGeneratedSources(sourceSets: NamedDomainObjectContainer<AndroidSourceSet>, project: Project) {
    val main = sourceSets.getByName("main")
    main.res.srcDirs(project.file(RES_GENERATED_OUTPUT_DIR))
    main.java.srcDirs(project.file(SOURCE_GENERATED_OUTPUT_DIR))
  }
}

internal data class ThemrStyle(val style: Style, val palette: String, val theme: String)

open class ThemrPluginExtension {
  var source: List<String> = listOf("themr")
  var combinations = emptyMap<String, List<String>>()
}

@CacheableTask
open class GenerateTask @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask() {

  @Input
  val combinations = objects.mapProperty(String::class.java, List::class.java)

  @InputFiles
  val paletteFiles = objects.fileCollection()

  @OutputDirectory
  val generatedSourceOutput = projectLayout.projectDirectory.dir(SOURCE_GENERATED_OUTPUT_DIR)

  @OutputDirectory
  val generatedResOutput = projectLayout.projectDirectory.dir(RES_GENERATED_OUTPUT_DIR)

  private val packageName = readPackageName(project)

  @TaskAction
  fun generate() {
    val styles = parseResourceStyles(paletteFiles)
    val themrStyles = createThemeCombinations(styles, combinations.get() as Map<String, List<String>>)

    writeGeneratedStyles(project, createOutputStyles(themrStyles))
    writeGeneratedSource(project, createThemR(packageName, themrStyles))
  }

  private fun writeGeneratedStyles(project: Project, stylesFileContents: String) {
    val directory = project.file("${RES_GENERATED_OUTPUT_DIR}/values")
    if (!directory.exists()) directory.mkdirs()
    File(directory, "gen-themr.xml").writeText(stylesFileContents)
  }

  private fun writeGeneratedSource(project: Project, javaFile: JavaFile) {
    val directory = project.file(SOURCE_GENERATED_OUTPUT_DIR)
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
    return project.plugins.findPlugin(AppPlugin::class.java)?.extension?.defaultConfig?.applicationId
        ?: findPackageNameFromManifest(project)
        ?: throw IllegalStateException("The project doesn't apply an android plugin!")
  }

  private fun findPackageNameFromManifest(project: Project): String? {
    val manifest = project.file("src/main/AndroidManifest.xml")
    val xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifest)
    xmlDoc.documentElement.normalize()
    return try {
      xmlDoc.getElementsByTagName("manifest").item(0).attributes.getNamedItem("package").nodeValue
    } catch (e: Exception) {
      null
    }
  }

}