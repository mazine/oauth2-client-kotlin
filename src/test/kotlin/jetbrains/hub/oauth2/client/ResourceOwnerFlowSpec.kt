package jetbrains.hub.oauth2.client

import jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import jetbrains.hub.oauth2.client.loader.TokenResponse
import jetbrains.hub.oauth2.client.source.RefreshableTokenSource
import org.jetbrains.spek.api.Spek
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResourceOwnerFlowSpec : Spek({
    describe("Resource Owner Flow") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val username = "user"
        val password = "secret"
        val scope = listOf("0-0-0-0-0", clientID)

        val getResourceOwnerFlow: OAuth2Client.() -> RefreshableTokenSource = {
            resourceOwnerFlow(tokenEndpoint, username, password, clientID, clientSecret, scope)
        }

        it("should access server with valid request if token is requested") {
            assertFlowIsCorrect(getResourceOwnerFlow) {
                assertEquals("https://hub.jetbrains.com/api/rest/oauth2/token?grant_type=password" +
                        "&username=user" +
                        "&password=secret" +
                        "&scope=0-0-0-0-0+$clientID", uri.toASCIIString())
                assertEquals("Basic MTIzNC0zMjEzLTMxMjM6dG9wc2VjcmV0", headers["Authorization"])
                assertNull(formParameters)
                TokenResponse.Success(
                        accessToken = "access-token",
                        refreshToken = null,
                        expiresIn = 3600,
                        requestTime = "2016-06-16 12:00:00".toCalendar(),
                        scope = listOf(clientID))
            }
        }

        it("should pass credentials as form parameters if required") {
            assertHeaderClientAuthSupported(clientID, clientSecret, {
                resourceOwnerFlow(tokenEndpoint, username, password, clientID, clientSecret, scope, ClientAuthTransport.FORM)
            })
        }

        it("shouldn't access server unless token is requested") {
            assertDoesntAccessServerUntilTokenIsRequested(getResourceOwnerFlow)
        }

        it("should cache token unless it is expired") {
            assertTokenCached(getResourceOwnerFlow)
        }

        it("should refresh token when it is expired") {
            assertExpiredTokenRefreshed(getResourceOwnerFlow)
        }
    }
})