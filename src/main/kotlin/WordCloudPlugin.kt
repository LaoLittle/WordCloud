package org.laolittle.plugin

import com.alibaba.druid.pool.DruidDataSource
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.verbose
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.laolittle.plugin.RecorderCompleter.Companion.todayTimeMillis
import java.io.File
import java.sql.Connection
import java.time.LocalDate
import java.util.*
import javax.sql.DataSource

object WordCloudPlugin : KotlinPlugin(
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
    private val wordCloudDir = dataFolder.resolve("WordCloud")
    const val eight = 8 * 60 * 60 * 1000L
    val time = (WordCloudConfig.time) * 60 * 60 * 1000L
    var bot: Bot? = null
    override fun onEnable() {
        val wordCloudPerm = registerPermission(
            "monitor",
            "生成词云"
        )
        val now = System.currentTimeMillis()
        val task = if ((now > (todayTimeMillis + eight)) && (now < (todayTimeMillis + time))) GroupMessageRecorder(
            wordCloudPerm
        )
        else RecorderCompleter(wordCloudPerm)
        task.run()
        WordCloudConfig.reload()
        logger.info { "配置文件已重载" }
        GlobalEventChannel.subscribeOnce<BotOnlineEvent> { this@WordCloudPlugin.bot = bot }
        GlobalEventChannel.subscribeGroupMessages {
            "今日词云" Here@{
                val dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
                val imageFile = File("$dataFolder/WordCloud").resolve("${group.id}_$dayWithYear")
                if (!imageFile.isFile) subject.sendMessage("还没有生成今日词云哦！${WordCloudConfig.time}点在来吧")
                else subject.sendImage(imageFile)
            }
        }

        val cacheTimer = object : TimerTask() {
            override fun run() {
                wordCloudDir.listFiles()?.forEach {
                    if (it.isFile) it.delete()
                }
                logger.verbose { "缓存已清理" }
            }
        }
        val aDay = 24 * 60 * 60 * 1000L
        Timer().schedule(cacheTimer, Date(todayTimeMillis + aDay), aDay)
    }

    private fun AbstractJvmPlugin.registerPermission(name: String, description: String) =
        PermissionService.INSTANCE.register(permissionId(name), description, parentPermission)

    init {
        dataSource.url = "jdbc:sqlite:$dataFolder/messageData.sqlite"
        dataSource.driverClassName = "org.sqlite.JDBC"
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE
        db = Database.connect(dataSource as DataSource)
        if (!wordCloudDir.isDirectory) wordCloudDir.mkdir()
    }
}