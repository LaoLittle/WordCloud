package org.laolittle.plugin

import com.huaban.analysis.jieba.JiebaSegmenter
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import org.laolittle.plugin.WordCloudPlugin as pluginMain

/**
 * 负责创建定时任务在8点启用[GroupMessageRecorder]并读取数据库绘制词云
 * @see WordCloudRenderer
 * */
class RecorderCompleter(
    private val perm: Permission,
    private val listener: Listener<*>? = null
) : TimerTask() {
    override fun run() {
        pluginMain.logger.info { "Drawer has been successfully started and waiting to start another recorder" }
        pluginMain.logger.info { "关闭监听器: ${listener?.complete() ?: false}" }
        val task = GroupMessageRecorder(perm)
        val aDay = 24 * 60 * 60 * 1000
        Timer().schedule(task, Date(todayTimeMillis + pluginMain.eight + aDay))
        val dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
        pluginMain.bot?.let {
            //.filter { everyGroup -> everyGroup.permitteeId.hasPermission(perm) }
            it.groups.forEach { group ->
                val table = MessageData(group.id)
                val sql: SqlExpressionBuilder.() -> Op<Boolean> = { table.time eq dayWithYear }
                val filePath = File("${pluginMain.dataFolder}/WordCloud").resolve("${group.id}_$dayWithYear")
                transaction(db = pluginMain.db) {
                    SchemaUtils.create(table)
                    val results = table.select(sql)
                    if (!results.empty()) {
                        val allContents = StringBuffer()
                        results.forEach { single ->
                            allContents.append(single[table.content])
                        }
                        val foo = JiebaSegmenter().process(allContents.toString(), JiebaSegmenter.SegMode.SEARCH)
                        val words = mutableListOf<String>()
                        foo.forEach { bar ->
                            words.add(bar.word)
                        }
                        val file = FileOutputStream(filePath)
                        file.write(WordCloudRenderer(words).wordCloud)
                        if (group.permitteeId.hasPermission(perm)) pluginMain.launch {
                            filePath.uploadAsImage(group).sendTo(group)
                        }
                    }
                    table.deleteWhere { table.time eq (dayWithYear - 2) }
                }
            }
        }
    }

    companion object {
        val todayTimeMillis: Long
            get() = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()
    }
}