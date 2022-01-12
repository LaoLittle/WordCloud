package org.laolittle.plugin

import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.verbose
import java.time.LocalDate
import java.util.*

/**
 * 创建监听器监听群消息事件并写入数据库, 并创建定时任务在[固定时间][WordCloudConfig.time]启用[RecorderCompleter]
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
        listener = GlobalEventChannel.parentScope(WordCloudPlugin).context(WordCloudPlugin.coroutineContext).subscribeAlways(
            priority = EventPriority.HIGHEST
        ) {
            MessageMonitorEvent(message, dayWithYear,subject).broadcast()
        }
        val task = RecorderCompleter(perm, listener)
        Timer().schedule(task, Date(RecorderCompleter.todayTimeMillis + WordCloudPlugin.time))
    }

    override fun cancel(): Boolean {
        return listener?.complete() ?: false
    }
}