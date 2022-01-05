package org.laolittle.plugin

import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.verbose
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate
import java.util.*

/**
 * 创建监听器监听群消息事件并写入数据库, 并创建定时任务在18点启用[RecorderCompleter]
 * */
class GroupMessageRecorder(
    private val perm: Permission
) : TimerTask() {
    private var listener: Listener<GroupMessageEvent>? = null
    override fun run() {
        val dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
        WordCloudPlugin.logger.info { "Recorder has been successfully started" }
        WordCloudPlugin.wordCloudDir.listFiles()?.forEach {
            if (it.isFile) it.delete()
        }
        WordCloudPlugin.logger.verbose { "缓存已清理" }
        listener = GlobalEventChannel.subscribeAlways(
            priority = EventPriority.MONITOR
        ) {
            val database = MessageData(subject.id)
            newSuspendedTransaction(Dispatchers.IO, WordCloudPlugin.db) {
                addLogger(MiraiSqlLogger)
                SchemaUtils.create(database)
                message.forEach { single ->
                    val filter =
                        (single is PlainText) && (!single.content.contains("请使用最新版手机QQ体验新功能")) && (single.content.isNotBlank())
                    if (filter)
                        database.insert { data ->
                            data[time] = dayWithYear
                            data[content] = single.content
                        }
                }
            }
        }
        val task = RecorderCompleter(perm, listener)
        Timer().schedule(task, Date(RecorderCompleter.todayTimeMillis + WordCloudPlugin.time))
    }

    override fun cancel(): Boolean {
        return listener?.complete() ?: false
    }
}