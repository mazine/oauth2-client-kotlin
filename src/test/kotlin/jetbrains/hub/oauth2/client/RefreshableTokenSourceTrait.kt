package jetbrains.hub.oauth2.client

import jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import jetbrains.hub.oauth2.client.loader.TokenResponse
import jetbrains.hub.oauth2.client.source.TokenSource
import org.jetbrains.spek.api.DescribeBody
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


fun DescribeBody.itShouldBeValidTokenSource(
        tokenEndpoint: URI,
        clientID: String, clientSecret: String,
        expectedFormParameters: Map<String, String>,
        getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    it("should parse access token correctly") {
        assertTokenIsParsedCorrectly(getFlow)
    }

    it("should call correct token endpoint") {
        assertTokenEndpoint(tokenEndpoint, getFlow)
    }

    it("should request token with correct form parameters") {
        assertFormParameters(expectedFormParameters, getFlow)
    }

    it("should pass credentials as header parameters if required") {
        assertHeaderClientAuthSupported(clientID, clientSecret, getFlow)
    }

    it("should pass credentials as form parameters if required") {
        assertFormClientAuthSupported(clientID, clientSecret, getFlow)
    }

    it("shouldn't access server unless token is requested") {
        assertDoesntAccessServerUntilTokenIsRequested(getFlow)
    }
}

fun DescribeBody.itShouldBeRefreshableTokenSource(
        getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    it("should cache token unless it is expired") {
        assertTokenCached(getFlow)
    }

    it("should refresh token when it is expired") {
        assertExpiredTokenRefreshed(getFlow)
    }
}

fun assertTokenIsParsedCorrectly(getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val tokenLoader = MockTokenLoader {
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf("0-0-0-0-0"))
    }
    val flow = OAuth2Client(tokenLoader).getFlow(ClientAuthTransport.HEADER)

    val accessToken = flow.accessToken
    assertEquals("access-token", accessToken.accessToken)
    assertEquals("2016-06-16 13:00:00", accessToken.expiresAt.asString())
    assertEquals(listOf("0-0-0-0-0"), accessToken.scope)

    assertEquals(1, tokenLoader.loadRecords.size)
}

fun assertTokenEndpoint(expectedTokenEndpoint: URI,
                        getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val flow = OAuth2Client(MockTokenLoader {
        assertEquals(expectedTokenEndpoint, uri)
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf())
    }).getFlow(ClientAuthTransport.HEADER)

    flow.accessToken
}

fun assertFormParameters(expectedFormParameters: Map<String, String>, getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val flow = OAuth2Client(MockTokenLoader {
        assertEquals(expectedFormParameters, this.formParameters)
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf())
    }).getFlow(ClientAuthTransport.HEADER)

    flow.accessToken
}

fun assertHeaderClientAuthSupported(clientID: String, clientSecret: String,
                                    getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val flow = OAuth2Client(MockTokenLoader {
        assertEquals("Basic ${Base64.encode("$clientID:$clientSecret".toByteArray())}", headers["Authorization"])
        assertNull(formParameters["client_id"])
        assertNull(formParameters["client_secret"])

        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf())
    }).getFlow(ClientAuthTransport.HEADER)

    flow.accessToken
}

fun assertFormClientAuthSupported(clientID: String, clientSecret: String,
                                  getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    val flow = OAuth2Client(MockTokenLoader {
        assertNull(headers["Authorization"])
        assertEquals(clientID, formParameters["client_id"])
        assertEquals(clientSecret, formParameters["client_secret"])
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf(clientID))
    }).getFlow(ClientAuthTransport.FORM)

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

    OAuth2Client(tokenLoader).getFlow(ClientAuthTransport.HEADER)
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

    val flow = OAuth2Client(tokenLoader).getFlow(ClientAuthTransport.HEADER)

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

    val flow = OAuth2Client(tokenLoader).getFlow(ClientAuthTransport.HEADER)

    flow.accessToken
    flow.accessToken

    assertEquals(2, tokenLoader.loadRecords.size)
}