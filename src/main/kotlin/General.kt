package org.laolittle.plugin

import net.mamoe.mirai.Bot

internal val wordCloudDir = WordCloudPlugin.dataFolder.resolve("WordCloud")

internal val backgroundDir = WordCloudPlugin.dataFolder.resolve("Background")

internal val bots by Bot.Companion::instances

internal fun buildWordCloud(block: WordCloudBuilder.() -> Unit) = WordCloudBuilder().apply(block).build()
