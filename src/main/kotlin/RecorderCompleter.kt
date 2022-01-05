package org.laolittle.plugin

import com.huaban.analysis.jieba.JiebaSegmenter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.utils.info
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.laolittle.plugin.JiebaSegmenter as JiebaObj
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import org.laolittle.plugin.WordCloudPlugin as pluginMain

/**
 * 负责创建定时任务在0点启用[GroupMessageRecorder]并读取数据库绘制词云
 * @see WordCloudRenderer
 * */
class RecorderCompleter(
    private val perm: Permission,
    private val listener: Listener<*>? = null
) : TimerTask() {
    private val aDay = 24 * 60 * 60 * 1000
    override fun run() {
        pluginMain.logger.info { "Drawer has been successfully started and waiting to start another recorder" }
        pluginMain.logger.info { "关闭监听器: ${listener?.complete() ?: false}" }
        val task = GroupMessageRecorder(perm)
        Timer().schedule(task, Date(todayTimeMillis + aDay))
        val dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
        pluginMain.bot.let {
            //.filter { everyGroup -> everyGroup.permitteeId.hasPermission(perm) }
            it.groups.forEach { group ->
                val table = MessageData(group.id)
                val sql: SqlExpressionBuilder.() -> Op<Boolean> = { table.time eq dayWithYear }
                val filePath = File("${pluginMain.dataFolder}/WordCloud").resolve("${group.id}_$dayWithYear")
                transaction(db = pluginMain.db) {
                    SchemaUtils.create(table)
                    val results = table.select(sql)
                    if (!results.empty()) {
                        val words = mutableListOf<String>()
                        results.forEach { single ->
                            val foo = JiebaObj.process(single[table.content], JiebaSegmenter.SegMode.SEARCH)
                            foo.forEach { bar ->
                                words.add(bar.word)
                            }
                        }
                        val file = FileOutputStream(filePath)
                        file.write(WordCloudRenderer(words).wordCloud)
                    }
                    table.deleteWhere { table.time eq (dayWithYear - 2) }
                }
            }
            pluginMain.launch {
                it.groups.filter { everyGroup -> everyGroup.permitteeId.hasPermission(perm) }.forEach { group ->
                    val filePath = File("${pluginMain.dataFolder}/WordCloud").resolve("${group.id}_$dayWithYear")
                    if (filePath.isFile) {
                        group.sendMessage("今日词云")
                        delay(500)
                        group.sendImage(filePath)
                        delay((300 ..3000L).random())
                    }
                }
            }
        }
    }

    companion object {
        val todayTimeMillis: Long
            get() = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()
    }
}