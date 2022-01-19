package org.laolittle.plugin

import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate
import java.time.LocalTime

object MessageRecorder : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.MessageRecorder",
        name = "MessageRecorder",
        version = "1.0-SNAPSHOT",
    ) {
        author("LaoLittle")
    }
) {

    override fun onEnable() {
        init()
        logger.info { "Plugin loaded" }
        globalEventChannel().subscribeAlways<GroupMessageEvent>(
            priority = MessageRecorderConfig.priority
        ) {
            val messageData = MessageData(subject.id)
            newSuspendedTransaction(Dispatchers.IO, database) {
                addLogger(MiraiSqlLogger)
                SchemaUtils.create(messageData)
                message.forEach { single ->
                    val filter =
                        (single is PlainText) && (!single.content.contains("请使用最新版手机QQ体验新功能")) && (single.content.isNotBlank())
                    if (filter)
                        messageData.insert { data ->
                            data[date] = LocalDate.now()
                            data[time] = "${(LocalTime.now().hour * 60) + LocalTime.now().minute}".toInt()
                            data[content] = single.content
                        }
                }
            }
        }
    }
    private fun init(){
        MessageRecorderConfig.reload()
    }
}