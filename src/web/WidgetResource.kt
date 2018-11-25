package com.ydhnwb.web

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ydhnwb.model.User
import com.ydhnwb.services.UserService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.webSocket

const val WIDGET_END_POINT = "/user"
val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }

fun Route.widget(userService: UserService){

    route(WIDGET_END_POINT){
        get("/"){ call.respond(userService.all()) }

        get("/{id}"){
            val widget = userService.getUser(call.parameters["id"]?.toInt()!!)
            if (widget == null){
                call.respond(HttpStatusCode.NotFound)
            }else{
                call.respond(widget)
            }
        }

        post("/") {
            val u = call.receive<User>()
            call.respond(HttpStatusCode.Created, userService.new(u))
        }

        put("/") {
            val u = call.receive<User>()
            val updated = userService.update(u)
            if(updated == null){
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK, updated)
            }
        }

        delete("/{id}") {
            val removed = userService.delete(call.parameters["id"]?.toInt()!!)
            if (removed) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound)
        }

        webSocket("/updates") {
            try {
                userService.addChangeListener(this.hashCode()) { p0 ->
                    outgoing.send(Frame.Text(mapper.writeValueAsString(p0)))
                }
                while(true) {
                    incoming.receiveOrNull() ?: break
                }
            } finally {
                userService.removeChangeListener(this.hashCode())
            }
        }
    }
}
