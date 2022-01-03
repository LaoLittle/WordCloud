package org.laolittle.plugin

import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.utils.info
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

class RecorderStarter(
    private val perm: Permission,
    private val listener: Listener<*>? = null
) : TimerTask() {
    override fun run() {
        WordCloud.logger.info { "Starter has been successfully started" }
        WordCloud.logger.info { "关闭监听器: ${listener?.complete() ?: false}" }
        val task = GroupMessageRecorder(perm)
        Timer().schedule(task, Date(todayTimeMillis + WordCloud.eight))
    }

    companion object {
        val todayTimeMillis: Long
            get() {
                return LocalDate.now().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()
            }
    }
}