package jetbrains.hub.oauth2.client

import jetbrains.hub.oauth2.client.loader.TokenResponse
import jetbrains.hub.oauth2.client.source.TokenSource
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

fun assertFlowIsCorrect(getFlow: OAuth2Client.() -> TokenSource,
                        onTokenRequest: MockTokenLoader.Request.() -> TokenResponse.Success) {
    var response: TokenResponse.Success? = null
    val tokenLoader = MockTokenLoader({
        onTokenRequest().apply {
            response = this
        }
    })
    val client = OAuth2Client(tokenLoader)

    val flow = client.getFlow()

    val accessToken = flow.accessToken

    assertEquals(response?.accessToken, accessToken.accessToken)
    assertEquals(response?.expiresAt?.asString(), accessToken.expiresAt.asString())
    assertEquals(response?.scope, accessToken.scope)

    assertEquals(1, tokenLoader.loadRecords.size)
}

fun assertHeaderClientAuthSupported(clientID: String, clientSecret: String,
                                    getFlow: OAuth2Client.() -> TokenSource) {
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

    val flow = client.getFlow()

    flow.accessToken
}

fun assertDoesntAccessServerUntilTokenIsRequested(getFlow: OAuth2Client.() -> TokenSource) {
    val tokenLoader = MockTokenLoader {
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = emptyList())
    }
    val client = OAuth2Client(tokenLoader)

    client.getFlow()
    assertTrue(tokenLoader.loadRecords.isEmpty())
}

