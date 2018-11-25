package com.ydhnwb.services

import com.ydhnwb.model.*
import com.ydhnwb.services.DatabaseHelper.dbQuery
import org.jetbrains.exposed.sql.*


class UserService {

    private val listeners = mutableMapOf<Int, suspend (com.ydhnwb.model.Notification<User?>) -> Unit>()

    fun removeChangeListener(id: Int) = listeners.remove(id)

    fun addChangeListener(id: Int, listener: suspend (Notification<User?>) -> Unit) {
        listeners[id] = listener
    }

    private suspend fun onChange(type: ChangeType, id: Int, entity: User?=null) {
        listeners.values.forEach {p0 ->
            p0.invoke(Notification(type, id, entity))
        }
    }

    suspend fun all(): List<User> = dbQuery { UsersTable.selectAll().map { p0 -> toModel(p0) } }

    suspend fun getUser(id: Int): User? = dbQuery {
        UsersTable.select {
            (UsersTable.id eq id)
        }.mapNotNull { p0 ->
            toModel(p0)
        }.singleOrNull()
    }

    suspend fun update(u: User): User? {
        val id = u.id
        return if (id == null) {
            new(u)
        } else {
            dbQuery {
                UsersTable.update({ UsersTable.id eq id }) {p0 ->
                    p0[name] = u.name
                    p0[phone] = u.phone
                    p0[email] = u.email
                    p0[last_edited] = System.currentTimeMillis()
                }
            }
            getUser(id).also {p0 ->
                onChange(ChangeType.UPDATE, id, p0)
            }
        }
    }

    suspend fun new(u: User): User {
        var key = 0
        dbQuery {
            key = (UsersTable.insert {p0 ->
                p0[name] = u.name
                p0[phone] = u.phone
                p0[email] = u.email
                p0[last_edited] = System.currentTimeMillis()
            } get UsersTable.id)!!
        }
        return getUser(key)!!.also {p0 ->
            onChange(ChangeType.CREATE, key, p0)
        }
    }

    suspend fun delete(id: Int): Boolean {
        return dbQuery {
            UsersTable.deleteWhere {
                UsersTable.id eq id
            } > 0
        }.also {p0 ->
            if(p0){
                onChange(ChangeType.DELETE, id)
            }
        }
    }

    private fun toModel(r: ResultRow): User {
        val u = User(name = r[UsersTable.name],
            phone = r[UsersTable.phone],
            email = r[UsersTable.email],
            last_edited = r[UsersTable.last_edited])
        u.id = r[UsersTable.id]
        return u
    }
}
