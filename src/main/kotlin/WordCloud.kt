package org.laolittle.plugin

import com.alibaba.druid.pool.DruidDataSource
import com.huaban.analysis.jieba.JiebaSegmenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.info
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.laolittle.plugin.RecorderStarter.Companion.todayTimeMillis
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
    val db: Database
    var dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
    const val eight = 8 * 60 * 60 * 1000L
    const val eighteen = 18 * 60 * 60 * 1000L
    override fun onEnable() {
        val wordCloudPerm = registerPermission(
            "monitor",
            "生成词云"
        )
        val now = System.currentTimeMillis()
        val task = if ((now > (todayTimeMillis + eight)) && (now < (todayTimeMillis + eighteen))) GroupMessageRecorder(
            wordCloudPerm
        )
        else RecorderStarter(wordCloudPerm)
        task.run()
        WordCloudConfig.reload()
        logger.info { "Plugin loaded" }
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
                delay(8 * 60 * 60 * 1000)
                logger.info { "time updated" }
                dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
            }
        }
        dataSource.url = "jdbc:sqlite:$dataFolder/messageData.sqlite"
        dataSource.driverClassName = "org.sqlite.JDBC"
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE
        db = Database.connect(dataSource as DataSource)
    }
}