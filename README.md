# themr [![CircleCI](https://circleci.com/gh/ouchadam/themr.svg?style=shield)](https://circleci.com/gh/ouchadam/attr) ![](https://img.shields.io/github/license/ouchadam/themr.svg) [ ![Download](https://api.bintray.com/packages/ouchadam/maven/themr/images/download.svg) ](https://bintray.com/ouchadam/maven/themr/_latestVersion)
Theme concatenation via gradle plugin


```gradle
buildscript {
  dependencies {
    classpath "com.github.ouchadam.themr:<latest version>"
  }
}

apply plugin: 'com.github.ouchadam.themr'

```

### Usage

res/values/themr.xml
```xml
<style name="PaletteA">
  <item name="brandColor">#008577</item>
</style>

<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
  <item name="colorPrimary">?attr/brandColor</item>
</style>
```

build.gradle
```groovy
themr {
  combinations = ["AppTheme": ["PaletteA", "PaletteB"]]
}
```

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
  setTheme(themr(R.style.PaletteB, R.style.AppTheme))
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_main)
}

private fun Activity.themr(paletteId: Int, themeId: Int): Int {
  val paletteStyle = this.resources.getResourceName(paletteId).split("/")[1]
  val themeStyle = this.resources.getResourceName(themeId).split("/")[1]
  return this.resources.getIdentifier(paletteStyle + "_" + themeStyle, "style", this.packageName)
}
```
