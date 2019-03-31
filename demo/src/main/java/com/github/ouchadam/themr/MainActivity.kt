package com.github.ouchadam.themr

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.github.ouchadam.attr.Attr
import com.github.ouchadam.attr.attr
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(themr(R.style.PaletteB, R.style.AppTheme))

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val attributes = theme.attr<ThemeAttributes>()
    bind(attributes)
  }

  private fun bind(attributes: ThemeAttributes) {
    bind(colorOne, attributes.colorPrimary)
    bind(colorTwo, attributes.colorAccent)
    bind(colorThree, attributes.backgroundInverse)
  }

  @SuppressLint("SetTextI18n")
  private fun bind(view: TextView, color: Int) {
    view.text = String.format("#%06X", 0xFFFFFF and color)
    view.setTextColor(color)
    view.addLeftSquare(color)
  }
}

private fun Activity.themr(paletteId: Int, themeId: Int): Int {
  val paletteStyle = this.resources.getResourceName(paletteId).split("/")[1]
  val themeStyle = this.resources.getResourceName(themeId).split("/")[1]
  return this.resources.getIdentifier(paletteStyle + "_" + themeStyle, "style", this.packageName)
}

@Attr
data class ThemeAttributes(
    @Attr.Id(R.attr.colorPrimary) @ColorInt val colorPrimary: Int,
    @Attr.Id(R.attr.colorAccent) @ColorInt val colorAccent: Int,
    @Attr.Id(R.attr.brandBackgroundColorInverse) @ColorInt val backgroundInverse: Int
)

private fun TextView.addLeftSquare(color: Int) {
  val drawable = PaintDrawable(color).apply {
    intrinsicWidth = 100
    intrinsicHeight = 100
  }
  this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
  this.compoundDrawablePadding = 20
}