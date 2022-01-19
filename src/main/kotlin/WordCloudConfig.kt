package org.laolittle.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.EventPriority
import java.awt.Font

object WordCloudConfig : AutoSavePluginConfig("Config") {
    @ValueDescription("最大单词")
    val max by value(60)

    @ValueDescription("最小单词")
    val min by value(10)

    @ValueDescription("词数限制")
    val wordLimit by value(500)

    @ValueDescription("指定字体文件 default为默认 (MiSans-Light)")
    val font by value("default")

    @ValueDescription("指定生成时间 (小时)")
    val time by value(18)

    @ValueDescription("""
        监听优先级
        顺序分别为 HIGHEST -> HIGH -> NORMAL -> LOW -> LOWEST -> MONITOR
        """)
    val priority by value(EventPriority.MONITOR)

    val fontOrigin: Array<Font> =
        (if (font == "default") Font.createFonts(WordCloudPlugin.javaClass.getResourceAsStream("/MiSans-Light.ttf"))
        else Font.createFonts(WordCloudPlugin.dataFolder.resolve(font)))
}