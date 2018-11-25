package com.ydhnwb.model

import org.jetbrains.exposed.sql.Table

object UsersTable : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 50)
    val phone = varchar("phone", 14)
    val email = varchar("email", 50)
    val last_edited = long("last_edited")
}


data class User(val name: String, val phone: String, val email : String, val last_edited: Long){
    var id : Int? = null
}
