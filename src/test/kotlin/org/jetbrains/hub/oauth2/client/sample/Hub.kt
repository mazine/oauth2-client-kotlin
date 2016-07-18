package org.jetbrains.hub.oauth2.client.sample

import org.jetbrains.hub.oauth2.client.jersey.oauth2Client
import java.net.URI

fun main(args: Array<String>) {
    val client = oauth2Client().clientFlow(
            URI("https://hub-staging.labs.intellij.net/api/rest/oauth2/token"),
            "client",
            "secret",
            listOf("0-0-0-0-0"))

    println(client.accessToken)
}