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
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

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
        val task = RecorderStarter(perm, listener)
        Timer().schedule(task, Date(RecorderStarter.todayTimeMillis + WordCloud.eighteen))
    }

    override fun cancel(): Boolean {
        return listener?.complete() ?: false
    }
}