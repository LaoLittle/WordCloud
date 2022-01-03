package org.laolittle.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import java.awt.Font

object WordCloudConfig : AutoSavePluginConfig("Config") {
    @ValueDescription("最大单词")
    val max by value(60)

    @ValueDescription("最小单词")
    val min by value(10)

    @ValueDescription("指定字体文件 default为默认 (微软雅黑)")
    val font by value("default")

    val fontOrigin: Array<Font> =
        (if (font == "default") Font.createFonts(WordCloud.javaClass.getResourceAsStream("/msyh.ttc"))
        else Font.createFonts(WordCloud.dataFolder.resolve(font)))
}