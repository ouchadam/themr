package com.github.ouchadam.themr

internal fun createOutputStyles(outputStyles: List<ThemrStyle>): String {
  return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
      "<resources>\n" + outputStyles.map {
    "<style name=\"${it.style.name}\" ${it.style.parent?.let { "parent=\"$it\"" }}>\n\t${it.style.items.joinToString("\n\t") {
      "<item name=\"${it.name}\">${it.value}</item>"
    }}\n</style>"
  }.joinToString("\n") + "\n</resources>"
}