package org.laolittle.plugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.event.EventPriority

object MessageRecorderConfig : AutoSavePluginConfig("MessageRecorderConfig") {
    @ValueDescription(
        """
        监听优先级
        顺序分别为 HIGHEST -> HIGH -> NORMAL -> LOW -> LOWEST -> MONITOR
        """
    )
    val priority by value(EventPriority.MONITOR)
}