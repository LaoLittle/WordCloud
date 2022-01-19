package org.laolittle.plugin

import com.alibaba.druid.pool.DruidDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import javax.sql.DataSource

val database by lazy {
    val dataSource = DruidDataSource()
    dataSource.url = "jdbc:sqlite:${MessageRecorder.dataFolder}/messageData.sqlite"
    dataSource.driverClassName = "org.sqlite.JDBC"
    TransactionManager.manager.defaultIsolationLevel =
        Connection.TRANSACTION_SERIALIZABLE
    Database.connect(dataSource as DataSource)
}