package jetbrains.hub.oauth2.client

import jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import jetbrains.hub.oauth2.client.source.RefreshableTokenSource
import org.jetbrains.spek.api.Spek
import java.net.URI

class CodeFlowSpek : Spek({
    describe("Code Flow") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val code = "SOME-CODE"
        val redirectURI = URI.create("https://localhost:8080")

        val getFlow: OAuth2Client.(ClientAuthTransport) -> RefreshableTokenSource = { authTransport ->
            codeFlow(tokenEndpoint, code, redirectURI, clientID, clientSecret, authTransport)
        }

        itShouldBeValidTokenSource(tokenEndpoint, clientID, clientSecret, mapOf(
                "grant_type" to "authorization_code",
                "code" to code,
                "redirect_uri" to redirectURI.toASCIIString()
        ), getFlow)
    }
})