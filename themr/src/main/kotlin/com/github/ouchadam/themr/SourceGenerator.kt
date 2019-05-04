package com.github.ouchadam.themr

import com.squareup.javapoet.*
import javax.lang.model.element.Modifier

internal fun createThemR(packageName: String, styles: List<ThemrStyle>): JavaFile {
  val innerMapType = ParameterizedTypeName.get(ClassName.get(Map::class.java), TypeName.INT.box(), TypeName.INT.box())
  val mapType = ParameterizedTypeName.get(ClassName.get(Map::class.java), TypeName.INT.box(), innerMapType)
  val cacheField = FieldSpec.builder(mapType, "THEME", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).build()

  val main = MethodSpec.methodBuilder("get")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(TypeName.INT)
      .addParameter(TypeName.INT, "paletteId")
      .addParameter(TypeName.INT, "themeId")
      .addStatement("return \$N.get(themeId).get(paletteId)", cacheField.name)
      .build()

  val mappingByThemeBlocks = styles.groupBy { it.theme }.entries.map {
    val themeKey = CodeBlock.builder()
        .addStatement("\$T \$N = new \$T()", Map::class.java, it.key, HashMap::class.java)
        .addAllStatements(it.value.map { themrStyle ->
          CodeBlock.of("\$N.put(R.style.${themrStyle.palette}, R.style.${themrStyle.style.name})", it.key)
        })
        .build()
    CodeBlock.builder()
        .add(themeKey)
        .addStatement("\$N.put(R.style.${it.key}, \$N)", cacheField.name, it.key)
        .build()
  }

  val initialiser = CodeBlock.builder()
      .addStatement("\$N = new \$T()", cacheField.name, HashMap::class.java)
      .also { builder -> mappingByThemeBlocks.forEach { builder.add(it) } }
      .build()

  val generatedClass = TypeSpec.classBuilder("ThemR")
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addField(cacheField)
      .addStaticBlock(initialiser)
      .addMethod(main)
      .build()

  return JavaFile.builder(packageName, generatedClass).build()
}

private fun CodeBlock.Builder.addAllStatements(blocks: List<CodeBlock>) = blocks.foldRight(this) { current, acc ->
  acc.add(current).add(";\n")
}