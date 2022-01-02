package org.laolittle.plugin

import org.jetbrains.exposed.sql.Table

class MessageData(groupId: Long) : Table("messages_$groupId") {
    val time = integer("time")
    val content = text("content")
}