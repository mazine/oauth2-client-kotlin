package jetbrains.hub.oauth2.client

import jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import jetbrains.hub.oauth2.client.loader.TokenResponse
import jetbrains.hub.oauth2.client.source.TokenSource
import org.jetbrains.spek.api.DescribeBody
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


fun DescribeBody.itShouldBeRefreshableTokenSource(
        clientID: String, clientSecret: String,
        getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {

    it("should pass credentials as form parameters if required") {
        assertHeaderClientAuthSupported(clientID, clientSecret, getFlow)
    }

    it("shouldn't access server unless token is requested") {
        assertDoesntAccessServerUntilTokenIsRequested(getFlow)
    }

    it("should cache token unless it is expired") {
        assertTokenCached(getFlow)
    }

    it("should refresh token when it is expired") {
        assertExpiredTokenRefreshed(getFlow)
    }
}

fun assertFlowIsCorrect(getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource,
                        onTokenRequest: MockTokenLoader.Request.() -> TokenResponse.Success) {
    var response: TokenResponse.Success? = null
    val tokenLoader = MockTokenLoader({
        onTokenRequest().apply {
            response = this
        }
    })
    val client = OAuth2Client(tokenLoader)

    val flow = client.getFlow(ClientAuthTransport.HEADER)

    val accessToken = flow.accessToken

    assertEquals(response?.accessToken, accessToken.accessToken)
    assertEquals(response?.expiresAt?.asString(), accessToken.expiresAt.asString())
    assertEquals(response?.scope, accessToken.scope)

    assertEquals(1, tokenLoader.loadRecords.size)
}

fun assertHeaderClientAuthSupported(clientID: String, clientSecret: String,
                                    getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val tokenLoader = MockTokenLoader {
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
    val client = OAuth2Client(tokenLoader)

    val flow = client.getFlow(ClientAuthTransport.FORM)

    flow.accessToken
}

fun assertDoesntAccessServerUntilTokenIsRequested(getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val tokenLoader = MockTokenLoader {
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = emptyList())
    }
    val client = OAuth2Client(tokenLoader)

    client.getFlow(ClientAuthTransport.HEADER)
    assertTrue(tokenLoader.loadRecords.isEmpty())
}

fun assertTokenCached(getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val tokenLoader = MockTokenLoader {
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = Calendar.getInstance(),
                scope = listOf())
    }
    val client = OAuth2Client(tokenLoader)

    val flow = client.getFlow(ClientAuthTransport.HEADER)

    flow.accessToken
    flow.accessToken
    flow.accessToken
    flow.accessToken

    assertEquals(1, tokenLoader.loadRecords.size)
}

fun assertExpiredTokenRefreshed(getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val tokenLoader = MockTokenLoader {
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf())
    }
    val client = OAuth2Client(tokenLoader)

    val flow = client.getFlow(ClientAuthTransport.HEADER)

    flow.accessToken
    flow.accessToken

    assertEquals(2, tokenLoader.loadRecords.size)
}