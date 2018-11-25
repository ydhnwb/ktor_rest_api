package com.ydhnwb.services

import com.ydhnwb.model.UsersTable
import com.ydhnwb.utils.Constants
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.coroutines.CoroutineContext


object DatabaseHelper {

    private val dispatcher: CoroutineContext

    init { dispatcher = newFixedThreadPoolContext(4, "database-pool") }

    fun init() {
        Database.connect(hikari())
        transaction {
            create(UsersTable)
            //Jika ingin memasukkan data dummy :
            /*UsersTable.insert {p0 ->
                p0[name] = "Prieyudha Akadita S"
                p0[phone] = "0812345678"
                p0[email] = "yudhanewbie@gmail.com"
                p0[last_edited] = System.currentTimeMillis()
            }
            UsersTable.insert {p0 ->
                p0[name] = "Markoplo"
                p0[phone] = "06666666"
                p0[email] = "mrkpl@gmail.com"
                p0[last_edited] = System.currentTimeMillis()
            }*/

        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = Constants.DRIVER_NAME
        // gunakan EMBEDDED_DATABASE buat simpan database di hdd
        // gunakan IN_MEMORY buat simpan database ke RAM
        // selengkapnya anda bisa baca-baca soal embedded database dan in memory database :)
        config.jdbcUrl = Constants.EMBEDDED_DATABASE
        config.maximumPoolSize = 3
        config.isAutoCommit = true //set ke false untuk redo semua data
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T = withContext(dispatcher) { transaction { block() } }
}