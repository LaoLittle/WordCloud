package org.laolittle.plugin

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.utils.verbose
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.laolittle.plugin.MessageDatabase.database
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
    private val perm: Permission
) : TimerTask() {
    private val aDay = 24 * 60 * 60 * 1000
    override fun run() {
        pluginMain.logger.verbose { "Drawer has been successfully started and waiting to start another recorder" }
        val task = GroupMessageRecorder(perm)
        Timer().schedule(task, Date(todayTimeMillis + aDay))
        val dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
        //.filter { everyGroup -> everyGroup.permitteeId.hasPermission(perm) }
        groups.forEach { id ->
            val table = MessageData(id)
            val sql: SqlExpressionBuilder.() -> Op<Boolean> = { table.date eq LocalDate.now() }
            val filePath = wordCloudDir.resolve("${id}_$dayWithYear")
            runBlocking {
                MessageDatabase.alsoLock {
                    transaction(database) {
                        SchemaUtils.create(table)
                        val results = table.select(sql)
                        if (!results.empty()) {
                            val words = mutableListOf<String>()
                            results.forEach { single ->
                                val foo = JiebaSegmenter.process(single[table.content], JiebaSegmenter.SegMode.SEARCH)
                                foo.forEach { bar ->
                                    words.add(bar.word)
                                }
                            }
                            val file = FileOutputStream(filePath)
                            file.write(WordCloudRenderer(words).wordCloud)
                        }
                        table.deleteWhere { table.date eq LocalDate.ofYearDay(LocalDate.now().year, LocalDate.now().dayOfYear - 2) }
                    }
                }
            }
        }
        pluginMain.launch {
            bots.forEach { bot ->
                bot.groups.filter { group -> group.permitteeId.hasPermission(perm) && group.id in groups }.forEach { group ->
                    val filePath = wordCloudDir.resolve("${group}_$dayWithYear")
                    if (filePath.isFile) {
                        group.sendMessage("今日词云")
                        delay(500)
                        group.sendImage(filePath)
                        delay((300..3000L).random())
                    }
                    groups.remove(group.id)
                }
            }
            bots.forEach {
                it.groups.forEach { g ->
                    groups.add(g.id)
                }
            }
        }
    }

    companion object {
        val todayTimeMillis: Long
            get() = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()
    }
}