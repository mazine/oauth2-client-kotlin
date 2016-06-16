package jetbrains.hub.oauth2.client

import jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import jetbrains.hub.oauth2.client.loader.TokenResponse
import org.jetbrains.spek.api.Spek
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClientFlowSpec : Spek({
    describe("Client Flow") {
        val tokenEndpoint = URI.create("https://hub.jetbrains.com/api/rest/oauth2/token")
        val clientID = "1234-3213-3123"
        val clientSecret = "topsecret"
        val scope = listOf("0-0-0-0-0", clientID)

        val tokenLoader = MockTokenLoader { throw IllegalStateException() }
        val oauth2Client = OAuth2Client(tokenLoader)

        beforeEach {
            tokenLoader.reset()
        }

        it("should access server with valid request if token is requested") {
            tokenLoader.onTokenRequest = {
                assertEquals("https://hub.jetbrains.com/api/rest/oauth2/token?grant_type=client_credentials&scope=0-0-0-0-0+$clientID", uri.toASCIIString())
                assertEquals("Basic MTIzNC0zMjEzLTMxMjM6dG9wc2VjcmV0", headers["Authorization"])
                assertNull(formParameters)
                TokenResponse.Success(
                        accessToken = "access-token",
                        refreshToken = null,
                        expiresIn = 3600,
                        requestTime = "2016-06-16 12:00:00".toCalendar(),
                        scope = listOf(clientID))
            }
            val clientFlow = oauth2Client.clientFlow(tokenEndpoint, clientID, clientSecret, scope)

            val accessToken = clientFlow.accessToken

            assertEquals("access-token", accessToken.accessToken)
            assertEquals("2016-06-16 13:00:00", accessToken.expiresAt.asString())
            assertEquals(listOf(clientID), accessToken.scope)

            assertEquals(1, tokenLoader.loadRecords.size)
        }

        it("should pass credentials as form parameters if required") {
            tokenLoader.onTokenRequest = {
                assertNull(headers["Authorization"])
                assertEquals(mapOf(
                        "client_id" to clientID,
                        "client_secret" to clientSecret), formParameters)
                TokenResponse.Success(
                        accessToken = "access-token",
                        refreshToken = null,
                        expiresIn = 3600,
                        requestTime = "2016-06-16 12:00:00".toCalendar(),
                        scope = listOf(clientID))
            }
            val clientFlow = oauth2Client.clientFlow(tokenEndpoint, clientID, clientSecret, scope, ClientAuthTransport.FORM)
            clientFlow.accessToken
        }

        it("shouldn't access server unless token is requested") {
            oauth2Client.clientFlow(tokenEndpoint, clientID, clientSecret, scope)
            assertTrue(tokenLoader.loadRecords.isEmpty())
        }

        it("should cache token unless it is expired") {
            tokenLoader.onTokenRequest = {
                TokenResponse.Success(
                        accessToken = "access-token",
                        refreshToken = null,
                        expiresIn = 3600,
                        requestTime = Calendar.getInstance(),
                        scope = listOf(clientID))
            }
            val clientFlow = oauth2Client.clientFlow(tokenEndpoint, clientID, clientSecret, scope)

            clientFlow.accessToken
            clientFlow.accessToken
            clientFlow.accessToken
            clientFlow.accessToken

            assertEquals(1, tokenLoader.loadRecords.size)
        }

        it("should refresh token when it is expired") {
            tokenLoader.onTokenRequest = {
                TokenResponse.Success(
                        accessToken = "access-token",
                        refreshToken = null,
                        expiresIn = 3600,
                        requestTime = "2016-06-16 12:00:00".toCalendar(),
                        scope = listOf(clientID))
            }
            val clientFlow = oauth2Client.clientFlow(tokenEndpoint, clientID, clientSecret, scope)

            clientFlow.accessToken
            clientFlow.accessToken

            assertEquals(2, tokenLoader.loadRecords.size)
        }
    }
})