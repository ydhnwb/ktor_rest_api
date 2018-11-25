package com.ydhnwb

import com.fasterxml.jackson.databind.SerializationFeature
import com.ydhnwb.services.DatabaseHelper
import com.ydhnwb.services.UserService
import com.ydhnwb.web.widget
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import io.ktor.websocket.WebSockets

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    DatabaseHelper.init()
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)
    install(ContentNegotiation){
        jackson { configure(SerializationFeature.INDENT_OUTPUT,true)}
    }
    install(Routing){ widget(userService = UserService()) }

}

//private suspend fun ApplicationCall.respondSucessJson(value: Boolean = true) = respond("""{"success":"${value}"}""")

/*private suspend fun <R> PipelineContext<*, ApplicationCall>.errorAware(block : suspend() -> R) : R? {
    return try{
        block()
    }catch (e : Exception){
        println("Exception Pipeline with message : ${e.message}")
        call.respondText("""{"error":"${e.message}"}""", ContentType.parse("application/json"), HttpStatusCode.InternalServerError)
        null
    }
}*/

