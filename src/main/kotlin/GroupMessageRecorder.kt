package org.laolittle.plugin

import net.mamoe.mirai.console.permission.Permission
import java.util.*

/**
 * 创建定时任务在[固定时间][WordCloudConfig.time]启用[RecorderCompleter]
 * */
class GroupMessageRecorder(
    private val perm: Permission
) : TimerTask() {
    override fun run() {
        val task = RecorderCompleter(perm)
        Timer().schedule(task, Date(RecorderCompleter.todayTimeMillis + WordCloudPlugin.time))
    }
}