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
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

/**
 * 负责创建定时任务在8点启用[GroupMessageRecorder]并读取数据库绘制词云
 * @see WordCloudRenderer
 * */
class RecorderCompleter(
    private val perm: Permission,
    private val listener: Listener<*>? = null
) : TimerTask() {
    override fun run() {
        WordCloud.logger.info { "Drawer has been successfully started and waiting to start another recorder" }
        WordCloud.logger.info { "关闭监听器: ${listener?.complete() ?: false}" }
        val task = GroupMessageRecorder(perm)
        val aDay = 24 * 60 * 60 * 1000
        Timer().schedule(task, Date(todayTimeMillis + WordCloud.eight + aDay))
        val dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
        WordCloud.bot?.let {
            it.groups.filter { forGroup -> forGroup.permitteeId.hasPermission(perm) }.forEach { group ->
                val table = MessageData(group.id)
                val sql: SqlExpressionBuilder.() -> Op<Boolean> = { table.time eq dayWithYear }
                transaction(db = WordCloud.db) {
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
                        WordCloudRenderer(words).wordCloud.use { res ->
                            WordCloud.launch {
                                group.sendMessage("今日词云")
                                delay(1_000)
                                group.sendImage(res)
                            }
                        }
                    }
                    table.deleteWhere { table.time eq (dayWithYear - 2) }
                }
            }
        }
    }

    companion object {
        val todayTimeMillis: Long
            get() {
                return LocalDate.now().atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli()
            }
    }
}