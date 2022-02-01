package org.laolittle.plugin

import net.mamoe.mirai.console.command.SimpleCommand

object SendWordCloud : SimpleCommand(
    WordCloudPlugin, "sendwc",
    description = "生成词云并推送"
) {

    @Handler
    fun send() {
        RecorderCompleter(WordCloudPlugin.wordCloudPerm).run()
    }
}