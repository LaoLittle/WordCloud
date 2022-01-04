package org.laolittle.plugin

import net.mamoe.mirai.utils.verbose
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs

object MiraiSqlLogger : SqlLogger {
    override fun log(context: StatementContext, transaction: Transaction) {
        WordCloudPlugin.logger.verbose { "SQL: ${context.expandArgs(transaction)}" }
    }
}