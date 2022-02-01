package org.laolittle.plugin

import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.WordFrequency
import com.kennycason.kumo.bg.RectangleBackground
import com.kennycason.kumo.font.FontWeight
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.FontScalar
import com.kennycason.kumo.font.scale.LinearFontScalar
import com.kennycason.kumo.palette.ColorPalette
import java.awt.Color
import java.awt.Dimension

class WordCloudBuilder {
    var dimension: Dimension = Dimension(600, 600)
    var collisionMode: CollisionMode = CollisionMode.PIXEL_PERFECT
    var kumoFont = KumoFont("Comic Sans MS", FontWeight.BOLD)
    var padding = 0
    var wordFrequencies = listOf<WordFrequency>()
    var background = RectangleBackground(dimension)
    var fontScalar: FontScalar = LinearFontScalar(10, 40)
    var backgroundColor: Color = Color.BLACK

    val colors = ColorsBuilder()

    fun colors(block: ColorsBuilder.() -> Unit) = colors.apply(block)

    fun build() = WordCloud(dimension, collisionMode).apply {
        setKumoFont(kumoFont)
        setPadding(padding)
        setColorPalette(
            ColorPalette(colors.build())
        )
        setBackground(background)
        setFontScalar(fontScalar)
        setBackgroundColor(backgroundColor)
        build(wordFrequencies)
    }
}

class ColorsBuilder {
    private val colors = mutableListOf<Color>()

    fun add(color: Color) = colors.add(color)

    operator fun Color.unaryPlus() = colors.add(this)

    fun build() = colors.toList()
}