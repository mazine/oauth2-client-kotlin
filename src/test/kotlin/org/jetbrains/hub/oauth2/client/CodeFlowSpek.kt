package org.jetbrains.hub.oauth2.client

import org.jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import org.jetbrains.hub.oauth2.client.source.TokenSource
import org.jetbrains.spek.api.Spek
import java.net.URI
import kotlin.test.assertEquals

class CodeFlowSpek : Spek({
    describe("Authentication URI") {
        val authEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/auth")
        val clientID = "1234-3213-3123"
        val redirectURI = URI.create("https://localhost:8080")
        val scopeElement = "0-0-0-0-0"
        val scope = listOf(scopeElement, clientID)
        val state = "some-unique-state-id"

        val authClient = OAuth2Client(MockTokenLoader { throw UnsupportedOperationException() })

        it("should form correct URI for online access token request") {
            assertEquals(
                    "https://hub.jetbrains.com/api/rest/oauth2/auth" +
                            "?response_type=code" +
                            "&client_id=$clientID" +
                            "&redirect_uri=https%3A%2F%2Flocalhost%3A8080" +
                            "&scope=$scopeElement+$clientID" +
                            "&state=$state",
                    authClient.codeFlowURI(
                            authEndpoint,
                            clientID, redirectURI,
                            scope, state, requestRefreshToken = false).toASCIIString()
            )
        }

        it("should form correct for URI offline access token request") {
            assertEquals(
                    "https://hub.jetbrains.com/api/rest/oauth2/auth" +
                            "?response_type=code" +
                            "&client_id=$clientID" +
                            "&redirect_uri=https%3A%2F%2Flocalhost%3A8080" +
                            "&scope=$scopeElement+$clientID" +
                            "&state=$state" +
                            "&access_type=offline",
                    authClient.codeFlowURI(
                            authEndpoint,
                            clientID, redirectURI,
                            scope, state, requestRefreshToken = true).toASCIIString()
            )
        }
    }

    describe("Token source") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val code = "SOME-CODE"
        val redirectURI = URI.create("https://localhost:8080")

        val getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource = { authTransport ->
            codeFlow(tokenEndpoint, code, redirectURI, clientID, clientSecret, authTransport)
        }

        itShouldBeValidTokenSource(tokenEndpoint, clientID, clientSecret, mapOf(
                "grant_type" to "authorization_code",
                "code" to code,
                "redirect_uri" to redirectURI.toASCIIString()
        ), getFlow)
    }

    describe("Refresh token") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val code = "SOME-CODE"
        val redirectURI = URI.create("https://localhost:8080")

        itShouldBeValidRefreshTokenSource(tokenEndpoint, clientID, clientSecret, mapOf(
                "grant_type" to "authorization_code",
                "code" to code,
                "redirect_uri" to redirectURI.toASCIIString()
        )) { authTransport ->
            codeRefreshToken(tokenEndpoint, code, redirectURI, clientID, clientSecret, authTransport)
        }
    }

})