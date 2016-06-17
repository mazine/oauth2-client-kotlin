package jetbrains.hub.oauth2.client

import jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import jetbrains.hub.oauth2.client.loader.TokenResponse
import jetbrains.hub.oauth2.client.source.RefreshableTokenSource
import org.jetbrains.spek.api.Spek
import java.net.URI
import kotlin.test.assertEquals

class ResourceOwnerFlowSpek : Spek({
    describe("Resource Owner Flow") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val username = "user"
        val password = "secret"
        val scope = listOf("0-0-0-0-0", clientID)

        val getFlow: OAuth2Client.(ClientAuthTransport) -> RefreshableTokenSource = {
            resourceOwnerFlow(tokenEndpoint, username, password, clientID, clientSecret, scope, it)
        }

        it("should access server with valid request if token is requested") {
            assertFlowIsCorrect(getFlow) {
                assertEquals("https://hub.jetbrains.com/api/rest/oauth2/token?grant_type=password" +
                        "&username=user" +
                        "&password=secret" +
                        "&scope=0-0-0-0-0+$clientID", uri.toASCIIString())
                TokenResponse.Success(
                        accessToken = "access-token",
                        refreshToken = null,
                        expiresIn = 3600,
                        requestTime = "2016-06-16 12:00:00".toCalendar(),
                        scope = listOf(clientID))
            }
        }

        itShouldBeRefreshableTokenSource(clientID, clientSecret, getFlow)
    }
})