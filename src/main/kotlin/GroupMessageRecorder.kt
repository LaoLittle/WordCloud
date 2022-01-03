package org.laolittle.plugin

import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.info
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

/**
 * 创建监听器监听群消息事件并写入数据库, 并创建定时任务在18点启用[RecorderCompleter]
 * */
class GroupMessageRecorder(
    private val perm: Permission
) : TimerTask() {
    private var listener: Listener<GroupMessageEvent>? = null
    override fun run() {
        WordCloud.logger.info { "Recorder has been successfully started" }
        listener = GlobalEventChannel.subscribeAlways(
            priority = EventPriority.MONITOR
        ) {
            if (subject.permitteeId.hasPermission(perm)) {
                val database = MessageData(subject.id)
                newSuspendedTransaction(Dispatchers.IO, WordCloud.db) {
                    addLogger(StdOutSqlLogger)
                    SchemaUtils.create(database)
                    message.forEach { single ->
                        val filter =
                            (single is PlainText) && (!single.content.contains("请使用最新版手机QQ体验新功能")) && (single.content.isNotBlank())
                        if (filter)
                            database.insert { data ->
                                data[time] = WordCloud.dayWithYear
                                data[content] = single.content
                            }
                    }
                }
            }
        }
        val task = RecorderCompleter(perm, listener)
        Timer().schedule(task, Date(RecorderCompleter.todayTimeMillis + WordCloud.time))
    }

    override fun cancel(): Boolean {
        return listener?.complete() ?: false
    }
}