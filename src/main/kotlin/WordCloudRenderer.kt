package org.laolittle.plugin

import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.bg.RectangleBackground
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.SqrtFontScalar
import com.kennycason.kumo.nlp.FrequencyAnalyzer
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer
import com.kennycason.kumo.palette.ColorPalette
import org.laolittle.plugin.WordCloudConfig.fontOrigin
import java.awt.Color
import java.awt.Dimension
import java.io.ByteArrayOutputStream

class WordCloudRenderer(private val words: List<String>) {
    val wordCloud: ByteArray
        get() = words.getWordCloud()

    private fun List<String>.getWordCloud(): ByteArray {
        val frequencyAnalyzer = FrequencyAnalyzer()
        frequencyAnalyzer.setWordFrequenciesToReturn(WordCloudConfig.wordLimit)
        frequencyAnalyzer.setMinWordLength(2)
        frequencyAnalyzer.setWordTokenizer(ChineseWordTokenizer())

        val wordFrequencyList = frequencyAnalyzer.load(this)
        val dimension = Dimension(600, 600)
        val wordCloud = WordCloud(dimension, CollisionMode.PIXEL_PERFECT)

        wordCloud.setKumoFont(KumoFont(fontOrigin.first()))
        wordCloud.setPadding(2)
        wordCloud.setColorPalette(
            ColorPalette(
                Color(0xed1941),
                Color(0xf26522),
                Color(0x845538),
                Color(0x8a5d19),
                Color(0x7f7522),
                Color(0x25B3DE),
                Color(0x5c7a29),
                Color(0x1d953f),
                Color(0x007d65),
                Color(0x65c294)
            )
        )
        //wordCloud.setBackground(CircleBackground(300))
        wordCloud.setBackground(RectangleBackground(dimension))
        wordCloud.setFontScalar(SqrtFontScalar(WordCloudConfig.min, WordCloudConfig.max))
        wordCloud.setBackgroundColor(Color(255, 255, 255))

        wordCloud.build(wordFrequencyList)
        val output = ByteArrayOutputStream()
        wordCloud.writeToStreamAsPNG(output)
        output.use { return it.toByteArray() }
    }
}