package org.jetbrains.hub.oauth2.client.jersey

import org.glassfish.jersey.client.JerseyClient
import org.glassfish.jersey.client.JerseyClientBuilder
import org.jetbrains.hub.oauth2.client.OAuth2Client
import org.jetbrains.hub.oauth2.client.loader.TokenLoader
import org.jetbrains.hub.oauth2.client.loader.TokenResponse
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*
import javax.ws.rs.client.Entity
import javax.ws.rs.core.Form
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder

class JerseyClientTokenLoader(val jerseyClient: JerseyClient = JerseyClientBuilder.createClient()) : TokenLoader {
    val log = LoggerFactory.getLogger(JerseyClientTokenLoader::class.java)

    override fun load(uri: URI, headers: Map<String, String>, formParameters: Map<String, String>): TokenResponse {
        val request = jerseyClient.target(uri).request().let { request ->
            headers.entries.fold(request, { currentRequest, header ->
                currentRequest.header(header.key, header.value)
            })
        }

        val requestTime = Calendar.getInstance()

        val response = request.post(Entity.form(Form().apply {
            formParameters.entries.forEach {
                param(it.key, it.value)
            }
        }), Response::class.java)

        return try {
            val entity = response.readEntity(Map::class.java)
            if (response.status == Response.Status.OK.statusCode) {
                TokenResponse.Success(
                        entity["access_token"] as String,
                        entity["refresh_token"] as? String,
                        entity["expires_in"] as Int,
                        requestTime,
                        (entity["scope"] as? String)?.split(' ') ?: emptyList()
                )
            } else {
                TokenResponse.Error(
                        entity["error"] as String,
                        entity["error_description"] as? String)
            }
        } catch (e: Exception) {
            log.info(e.message, e)
            TokenResponse.Error("unknown_error", e.message)
        }

    }

    override fun authURI(uri: URI, queryParameters: Map<String, String>): URI {
        return UriBuilder.fromUri(uri).apply {
            queryParameters.keys.forEach {
                queryParam(it, "{$it}")
            }
        }.buildFromMap(queryParameters)
    }
}

fun oauth2Client() = OAuth2Client(JerseyClientTokenLoader())