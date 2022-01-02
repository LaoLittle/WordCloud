package org.laolittle.plugin

import com.alibaba.druid.pool.DruidDataSource
import com.huaban.analysis.jieba.JiebaSegmenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.info
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.LocalDate
import javax.sql.DataSource

object WordCloud : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.WordCloud",
        name = "WordCloud",
        version = "1.0",
    ) {
        author("LaoLittle")
    }
) {
    private val dataSource = DruidDataSource()
    private val db: Database
    private var dayAndYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
    override fun onEnable() {
        val wordCloudPerm = registerPermission(
            "monitor",
            "生成词云"
        )
        logger.info { "Plugin loaded" }
        GlobalEventChannel.subscribeAlways<GroupMessageEvent>(
            priority = EventPriority.MONITOR
        ) {
            if (subject.permitteeId.hasPermission(wordCloudPerm)) {
                val database = MessageData(subject.id)
                transaction(db) {
                    SchemaUtils.create(database)
                    message.forEach { single ->
                        val filter =
                            (single is PlainText) && (!single.content.contains("请使用最新版手机QQ体验新功能")) && (single.content.isNotBlank())
                        if (filter)
                            database.insert { data ->
                                data[time] = dayAndYear
                                data[content] = single.content
                            }
                    }
                }
            }
        }
        GlobalEventChannel.subscribeMessages {
            "tet"{
                subject.sendMessage("plz input")
                val inputMessage = nextMessage(30_000)
                val foo = JiebaSegmenter().process(inputMessage.content, JiebaSegmenter.SegMode.SEARCH)
                val words = mutableListOf<String>()
                foo.forEach {
                    words.add(it.word)
                }
                WordCloudDrawer(words).wordCloud.use { subject.sendImage(it) }
            }
        }
    }

    private fun AbstractJvmPlugin.registerPermission(name: String, description: String) =
        PermissionService.INSTANCE.register(permissionId(name), description, parentPermission)


    init {
        launch(Dispatchers.IO) {
            while (true) {
                delay(2 * 60 * 60 * 1000)
                logger.info { "time updated" }
                dayAndYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
            }
        }
        dataSource.url = "jdbc:sqlite:$dataFolder/messageData.sqlite"
        dataSource.driverClassName = "org.sqlite.JDBC"
        db = Database.connect(dataSource as DataSource)
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE
        WordCloudConfig.reload()
    }
}