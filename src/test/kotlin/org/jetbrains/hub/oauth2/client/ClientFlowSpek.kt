package org.jetbrains.hub.oauth2.client

import org.jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import org.jetbrains.hub.oauth2.client.source.RefreshableTokenSource
import org.jetbrains.spek.api.Spek
import java.net.URI

class ClientFlowSpek : Spek({
    describe("Token source") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val scopeElement = "0-0-0-0-0"
        val scope = listOf(scopeElement, clientID)

        val getFlow: OAuth2Client.(ClientAuthTransport) -> RefreshableTokenSource = {
            clientFlow(tokenEndpoint, clientID, clientSecret, scope, it)
        }

        itShouldBeValidTokenSource(tokenEndpoint, clientID, clientSecret, mapOf(
                "grant_type" to "client_credentials",
                "scope" to "$scopeElement $clientID"
        ), getFlow)

        itShouldBeRefreshableTokenSource(getFlow)
    }
})