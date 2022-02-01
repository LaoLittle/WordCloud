package org.laolittle.plugin

import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.bg.RectangleBackground
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.SqrtFontScalar
import com.kennycason.kumo.nlp.FrequencyAnalyzer
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer
import java.awt.Color
import java.awt.Dimension
import java.io.ByteArrayOutputStream

class WordCloudRenderer(private val words: List<String>) {
    val wordCloud: ByteArray
        get() = words.getWordCloud()

    private fun List<String>.getWordCloud(): ByteArray {
        val frequencyAnalyzer = FrequencyAnalyzer().apply {
            setWordFrequenciesToReturn(WordCloudConfig.wordLimit)
            setMinWordLength(2)
            setWordTokenizer(ChineseWordTokenizer())
        }

        val wordCloud = buildWordCloud {
            dimension = Dimension(600, 600)
            collisionMode = CollisionMode.PIXEL_PERFECT
            padding = 2
            kumoFont = KumoFont(WordCloudConfig.fontOrigin.first())
            colors {
                arrayOf(
                    Color(0xf26522),
                    Color(0x845538),
                    Color(0x8a5d19),
                    Color(0x7f7522),
                    Color(0x25B3DE),
                    Color(0x5c7a29),
                    Color(0x1d953f),
                    Color(0x007d65),
                    Color(0x65c294)
                ).forEach(this::add)
            }
            background = RectangleBackground(Dimension(600, 600))
            fontScalar = SqrtFontScalar(WordCloudConfig.min, WordCloudConfig.max)
            backgroundColor = Color(255, 255, 255)
            wordFrequencies = frequencyAnalyzer.load(this@getWordCloud)
        }
        //wordCloud.setBackground(CircleBackground(300))

        val output = ByteArrayOutputStream()
        wordCloud.writeToStreamAsPNG(output)
        output.use { return it.toByteArray() }
    }
}