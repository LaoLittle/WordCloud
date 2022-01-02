package org.laolittle.plugin

import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.bg.RectangleBackground
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.SqrtFontScalar
import com.kennycason.kumo.nlp.FrequencyAnalyzer
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer
import com.kennycason.kumo.palette.ColorPalette
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.io.ByteArrayOutputStream
import org.laolittle.plugin.WordCloud as pluginMain

class WordCloudAnn(private val words: List<String>) {
    val wordCloud: ExternalResource get() {
       return getWordCloud(words).toExternalResource()
    }

    private fun getWordCloud(words: List<String>): ByteArray {
        val frequencyAnalyzer = FrequencyAnalyzer()
        frequencyAnalyzer.setWordFrequenciesToReturn(100)
        frequencyAnalyzer.setMinWordLength(2)
        frequencyAnalyzer.setWordTokenizer(ChineseWordTokenizer())
        val wordFrequencyList = frequencyAnalyzer.load(words)

        val dimension = Dimension(600, 600)

        val wordCloud = WordCloud(dimension, CollisionMode.PIXEL_PERFECT)
        val font = Font.createFonts(pluginMain.dataFolder.resolve(WordCloudConfig.font))
        wordCloud.setKumoFont(KumoFont(font[0]))
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
       // wordCloud.setBackground(CircleBackground(300))
        wordCloud.setBackground(RectangleBackground(dimension))
        wordCloud.setFontScalar(SqrtFontScalar(WordCloudConfig.min, WordCloudConfig.max))
        wordCloud.setBackgroundColor(Color(255, 255, 255))

        wordCloud.build(wordFrequencyList)
        val output = ByteArrayOutputStream()
        wordCloud.writeToStream("png", output)
        return output.toByteArray()
    }
}