package org.laolittle.plugin

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

class MessageData(groupId: Long) : Table("messages_$groupId") {
    val date = date("date")
    val time = integer("time")
    val content = varchar("content", 4500)
}