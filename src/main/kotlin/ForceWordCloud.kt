package org.laolittle.plugin

import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.console.command.CommandSenderOnMessage
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.laolittle.plugin.MessageDatabase.database
import java.time.LocalDate

object ForceWordCloud : SimpleCommand(
    WordCloudPlugin, "fwc", "强制生成词云",
    description = "强制生成词云"
) {
    @OptIn(ExperimentalCommandDescriptors::class, ConsoleExperimentalApi::class)
    override val prefixOptional: Boolean = true

    @Handler
    suspend fun CommandSenderOnMessage<GroupMessageEvent>.render() {
        val table = MessageData(fromEvent.subject.id)
        val sql: SqlExpressionBuilder.() -> Op<Boolean> = { table.date eq LocalDate.now() }
        newSuspendedTransaction(Dispatchers.IO, db = database) {
            SchemaUtils.create(table)
            val results = table.select(sql)
            if (!results.empty()) {
                val words = mutableListOf<String>()
                results.forEach { single ->
                    val foo = JiebaSegmenter.process(
                        single[table.content],
                        JiebaSegmenter.SegMode.SEARCH
                    )
                    foo.forEach { bar ->
                        words.add(bar.word)
                    }
                }
                WordCloudRenderer(words).wordCloud.toExternalResource().use { fromEvent.subject.sendImage(it) }
            }
        }

    }
}