# themr [![CircleCI](https://circleci.com/gh/ouchadam/themr.svg?style=shield)](https://circleci.com/gh/ouchadam/themr) ![](https://img.shields.io/github/license/ouchadam/themr.svg) [ ![Download](https://api.bintray.com/packages/ouchadam/maven/themr/images/download.svg) ](https://bintray.com/ouchadam/maven/themr/_latestVersion)
Theme concatenation via gradle plugin


```gradle
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath "com.github.ouchadam.themr:<latest-version>"
  }
}

apply plugin: 'com.android.application'
apply plugin: 'com.github.ouchadam.themr'

```

### Usage

res/values/themr.xml
```xml
<style name="PaletteLight">
  <item name="brandColor">#008577</item>
</style>

<style name="PaletteDark">
  <item name="brandColor">#000000</item>
</style>

<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
  <item name="colorPrimary">?attr/brandColor</item>
</style>
```

build.gradle
```groovy
themr {
  combinations = ["AppTheme": ["PaletteLight", "PaletteDark"]]
}
```

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
  setTheme(themr(R.style.PaletteDark, R.style.AppTheme))
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_main)
}

private fun Activity.themr(paletteId: Int, themeId: Int): Int {
  val paletteStyle = this.resources.getResourceName(paletteId).split("/")[1]
  val themeStyle = this.resources.getResourceName(themeId).split("/")[1]
  return this.resources.getIdentifier(paletteStyle + "_" + themeStyle, "style", this.packageName)
}
```

### What is this?

A gradle plugin to generate combinations of themes based on a color palette. This is done by simplying inlining a palette style into a theme style.

The example above will generate

```xml
<style name="PaletteLight_AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
  <item name="brandColor">#008577</item>
  <item name="colorPrimary">?attr/brandColor</item>
</style>

<style name="PaletteDark_AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
  <item name="brandColor">#000000</item>
  <item name="colorPrimary">?attr/brandColor</item>
</style>
```



