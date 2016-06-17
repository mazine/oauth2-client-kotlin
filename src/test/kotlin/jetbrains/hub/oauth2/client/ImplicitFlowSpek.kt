package jetbrains.hub.oauth2.client

import org.jetbrains.spek.api.Spek
import java.net.URI
import kotlin.test.assertEquals

class ImplicitFlowSpek : Spek({
    describe("Authentication URI") {
        val authEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/auth")
        val clientID = "1234-3213-3123"
        val redirectURI = URI.create("https://localhost:8080")
        val scopeElement = "0-0-0-0-0"
        val scope = listOf(scopeElement, clientID)
        val state = "some-unique-state-id"

        val authClient = OAuth2Client(MockTokenLoader { throw UnsupportedOperationException() })

        it("should form correct URI") {
            assertEquals(
                    "https://hub.jetbrains.com/api/rest/oauth2/auth" +
                            "?response_type=token" +
                            "&client_id=$clientID" +
                            "&redirect_uri=https%3A%2F%2Flocalhost%3A8080" +
                            "&scope=$scopeElement+$clientID" +
                            "&state=$state",
                    authClient.implicitFlowURI(
                            authEndpoint,
                            clientID, redirectURI,
                            scope, state).toASCIIString()
            )
        }
    }
})