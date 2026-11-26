package com.clipflow.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.clipflow.Database

class TestDriverFactory : DriverFactory {
    override val dbName: String = "clipflow_test.db"
    override var sqlDriver: SqlDriver? = null

    override fun createDriver(): SqlDriver {
        sqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(sqlDriver!!)
        return sqlDriver!!
    }

    override fun closeDriver() {
        sqlDriver?.close()
        sqlDriver = null
    }
}
