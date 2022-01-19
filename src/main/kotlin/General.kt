package org.laolittle.plugin

import net.mamoe.mirai.Bot

val wordCloudDir = WordCloudPlugin.dataFolder.resolve("WordCloud")

val groups = mutableSetOf<Long>()

val bots = mutableSetOf<Bot>()