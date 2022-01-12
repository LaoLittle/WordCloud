package org.laolittle.plugin

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.message.data.MessageChain

data class MessageMonitorEvent(
val message: MessageChain,
val dayWithYear: Int,
val subject: Group
) : AbstractEvent()