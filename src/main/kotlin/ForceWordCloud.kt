package org.laolittle.plugin

import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate

object ForceWordCloud : SimpleCommand(
    WordCloudPlugin, "fwc", "强制生成词云",
    description = "强制生成词云"
) {
    @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
    override val prefixOptional: Boolean = true

    @Handler
    suspend fun CommandSenderOnMessage<GroupMessageEvent>.render(){
        val dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
        val table = MessageData(fromEvent.subject.id)
        val sql: SqlExpressionBuilder.() -> Op<Boolean> = { table.time eq dayWithYear }
        newSuspendedTransaction(Dispatchers.IO ,db = WordCloudPlugin.db) {
            SchemaUtils.create(table)
            val results = table.select(sql)
            if (!results.empty()) {
                val words = mutableListOf<String>()
                results.forEach { single ->
                    val foo = JiebaSegmenter.process(single[table.content], com.huaban.analysis.jieba.JiebaSegmenter.SegMode.SEARCH)
                    foo.forEach { bar ->
                        words.add(bar.word)
                    }
                }
                WordCloudRenderer(words).wordCloud.toExternalResource().use { fromEvent.subject.sendImage(it) }
            }
            table.deleteWhere { table.time eq (dayWithYear - 2) }
        }

    }
}