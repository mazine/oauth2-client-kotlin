package org.jetbrains.hub.oauth2.client

import org.jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import org.jetbrains.spek.api.Spek
import java.net.URI

class ResourceOwnerFlowSpek : Spek({
    describe("Token source") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val username = "user"
        val password = "secret"
        val scopeElement = "0-0-0-0-0"
        val scope = listOf(scopeElement, clientID)

        val getFlow: OAuth2Client.(ClientAuthTransport) -> AccessTokenSource = { authTransport ->
            resourceOwnerFlow(tokenEndpoint, username, password, clientID, clientSecret, scope, authTransport)
        }

        itShouldBeValidTokenSource(tokenEndpoint, clientID, clientSecret, mapOf(
                "grant_type" to "password",
                "username" to username,
                "password" to password,
                "scope" to "$scopeElement $clientID"
        ), { getFlow(it).accessToken })

        itShouldBeRefreshableTokenSource(getFlow)
    }

    describe("Refresh token") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val username = "user"
        val password = "secret"
        val scopeElement = "0-0-0-0-0"
        val scope = listOf(scopeElement, clientID)


        itShouldBeValidRefreshTokenSource(tokenEndpoint, clientID, clientSecret, mapOf(
                "grant_type" to "password",
                "username" to username,
                "password" to password,
                "scope" to "$scopeElement $clientID",
                "access_type" to "offline"
        )) { authTransport ->
            resourceOwnerRefreshToken(tokenEndpoint, username, password, clientID, clientSecret, scope, authTransport)
        }
    }
})