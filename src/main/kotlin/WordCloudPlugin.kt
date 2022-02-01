package org.laolittle.plugin

import io.ktor.util.date.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import org.laolittle.plugin.RecorderCompleter.Companion.todayTimeMillis
import java.time.LocalDate

object WordCloudPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.WordCloud",
        name = "WordCloud",
        version = "1.1",
    ) {
        author("LaoLittle")

        dependsOn(
            PluginDependency("org.laolittle.plugin.MessageRecorder", ">= 1.1", true),
            PluginDependency("org.laolittle.plugin.implementation", ">= 1.0.1", true)
        )
    }
) {
    lateinit var wordCloudPerm: Permission

    override fun onEnable() {
        wordCloudPerm = registerPermission(
            "monitor",
            "生成词云"
        )

        val osName = System.getProperties().getProperty("os.name")
        if (!osName.startsWith("Windows")) {
            logger.info { "检测到当前为${osName}系统，将使用headless模式" }
            System.setProperty("java.awt.headless", "true")
        }
        WordCloudConfig.reload()
        ForceWordCloud.register()
        SendWordCloud.register()
        val task = if (getTimeMillis() < (todayTimeMillis + this.time)) GroupMessageRecorder(wordCloudPerm)
        else RecorderCompleter(wordCloudPerm)
        task.run()
        logger.info { "配置文件已重载" }
        globalEventChannel().subscribeGroupMessages {
            "今日词云" Here@{
                val dayWithYear = "${LocalDate.now().year}${LocalDate.now().dayOfYear}".toInt()
                val imageFile = wordCloudDir.resolve("${group.id}_$dayWithYear")
                if (getTimeMillis() < (todayTimeMillis + WordCloudPlugin.time)) {
                    subject.sendMessage("还没有生成今日词云哦！${WordCloudConfig.time}点在来吧")
                    return@Here
                }
                if (imageFile.isFile) subject.sendImage(imageFile)
                else subject.sendMessage("貌似没办法生成词云呢，让群活跃一点好不好")
            }
        }
    }

    private fun AbstractJvmPlugin.registerPermission(name: String, description: String) =
        PermissionService.INSTANCE.register(permissionId(name), description, parentPermission)

    val time get() = (WordCloudConfig.time) * 60 * 60 * 1000L

    init {
        if (!wordCloudDir.isDirectory) wordCloudDir.mkdir()
    }
}