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

### Why is this useful?


When an app uses mulitple themes 

```xml
<style name="HomeTheme" parent="Theme.AppCompat.Light.NoActionBar">
    <item name="colorPrimary">@color/blue</item>
    <item name="android:textColor">@color/black</item>
</style>

<style name="DetailsTheme" parent="Theme.AppCompat.Light.NoActionBar">
    <item name="colorPrimary">@color/red</item>
    <item name="android:textColor">@color/white</item>
</style>
```

and different palettes are required such as a dark mode, management starts to get tricky, especially when more palettes are introduced. This can be achieved by copying all of the themes for each palette, rearchitecting the theme hierarchy or by programatically applying the colours. 


```xml
<style name="HomeTheme" parent="Theme.AppCompat.Light.NoActionBar">
    <item name="colorPrimary">@color/blue</item>
    <item name="android:textColor">@color/black</item>
</style>

<style name="DarkHomeTheme" parent="Theme.AppCompat.Light.NoActionBar">
    <item name="colorPrimary">@color/dark_blue</item>
    <item name="android:textColor">@color/grey</item>
</style>
```

```kotlin
fun bindView(view) {
  val themeWrapper = createThemeWrapperFor(Configuration.DARK_MODE) // find all the dark mode attributes
  view.text.setTextColor(themeWrapper.colorPrimary)
}
```

`themr` reduces the need for this boiler plate by allowing the palettes and themes to be decoupled and the combinations auto generated.


Combintations are declared as part of the plugin extension
```groovy
themr {
  combinations = [
    "HomeTheme": ["LightMode", "DarkMode"],
    "DetailsTheme": ["LightMode", "DarkMode"]
  ]
}
```

```xml
<style name="LightMode">
    <item name="brandColor">@color/blue</item>
    <item name="brandColorSecondary">@color/red</item>
    <item name="brandTextColor">@color/black</item>
    <item name="brandTextColorInverse">@color/white</item>
</style>

<style name="DarkMode">
    <item name="brandColor">@color/dark_blue</item>
    <item name="brandColorSecondary">@color/blue</item>
    <item name="brandTextColor">@color/grey</item>
    <item name="brandTextColorInverse">@color/black</item>
</style>

<style name="HomeTheme" parent="Theme.AppCompat.Light.NoActionBar">
    <item name="colorPrimary">?attr/brandColor</item>
    <item name="android:textColor">?attr/brandTextColor</item>
</style>

<style name="DetailsTheme" parent="Theme.AppCompat.Light.NoActionBar">
    <item name="colorPrimary">?attr/brandColorSecondary</item>
    <item name="android:textColor">?attr/brandTextColorInverse</item>
</style>
```

`themr` will generate styles to `buildDir/generated/res/themer` which the app can then consume via id or by `resources.getIdentifier`

```
{Palette}_{Theme}
LightMode_HomeTheme
DarkMode_HomeTheme
LightMode_DetailsTheme
DarkMode_DetailsTheme
```
