package org.jetbrains.hub.oauth2.client

import org.jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import org.jetbrains.hub.oauth2.client.source.RefreshableTokenSource
import org.jetbrains.spek.api.Spek
import java.net.URI

class RefreshTokenFlowSpek : Spek({
    describe("Token source") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val refreshToken = "SOME-CODE"
        val scopeElement = "0-0-0-0-0"
        val scope = listOf(scopeElement, clientID)

        val getFlow: OAuth2Client.(ClientAuthTransport) -> RefreshableTokenSource = { authTransport ->
            refreshTokenFlow(tokenEndpoint, refreshToken, clientID, clientSecret, scope, authTransport)
        }

        itShouldBeValidTokenSource(tokenEndpoint, clientID, clientSecret, mapOf(
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken,
                "scope" to "$scopeElement $clientID"
        ), getFlow)

        itShouldBeRefreshableTokenSource(getFlow)
    }
})