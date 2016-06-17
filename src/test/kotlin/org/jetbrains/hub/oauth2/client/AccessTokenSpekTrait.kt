package org.jetbrains.hub.oauth2.client

import org.jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import org.jetbrains.hub.oauth2.client.loader.TokenResponse
import org.jetbrains.spek.api.DescribeBody
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull


fun DescribeBody.itShouldBeValidTokenSource(
        expectedTokenEndpoint: URI,
        clientID: String, clientSecret: String,
        expectedFormParameters: Map<String, String>,
        getAccessToken: OAuth2Client.(ClientAuthTransport) -> AccessToken) {
    it("should parse access token correctly") {
        val (accessToken, tokenLoader) = createTokenSourceAndLoader(getAccessToken, TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf("0-0-0-0-0"))
        )
        assertEquals("access-token", accessToken.accessToken)
        assertEquals("2016-06-16 13:00:00", accessToken.expiresAt.asString())
        assertEquals(listOf("0-0-0-0-0"), accessToken.scope)

        assertEquals("Bearer access-token", accessToken.header.value)

        assertEquals(1, tokenLoader.loadRecords.size)
    }

    it("should call correct token endpoint") {
        requestToken({ getAccessToken(ClientAuthTransport.HEADER) }) {
            assertEquals(expectedTokenEndpoint, uri)
        }
    }

    it("should request token with correct form parameters") {
        requestToken({ getAccessToken(ClientAuthTransport.HEADER) }) {
            assertEquals(expectedFormParameters, this.formParameters)
        }
    }

    it("should pass credentials as header parameters if required") {
        requestToken({ getAccessToken(ClientAuthTransport.HEADER) }) {
            assertEquals("Basic ${Base64.encode("$clientID:$clientSecret".toByteArray())}", headers["Authorization"])
            assertNull(formParameters["client_id"])
            assertNull(formParameters["client_secret"])
        }
    }

    it("should pass credentials as form parameters if required") {
        requestToken({ getAccessToken(ClientAuthTransport.FORM) }) {
            assertNull(headers["Authorization"])
            assertEquals(clientID, formParameters["client_id"])
            assertEquals(clientSecret, formParameters["client_secret"])
        }
    }
}

private fun requestToken(
        getAccessToken: OAuth2Client.() -> AccessToken,
        assertTokenRequest: MockTokenLoader.Request.() -> Unit) {
    OAuth2Client(MockTokenLoader {
        assertTokenRequest()
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf("0-0-0-0-0"))

    }).getAccessToken()
}

private fun createTokenSourceAndLoader(
        getAccessToken: OAuth2Client.(ClientAuthTransport) -> AccessToken,
        tokenResponse: TokenResponse = TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = Calendar.getInstance(),
                scope = listOf())): Pair<AccessToken, MockTokenLoader> {
    val tokenLoader = MockTokenLoader { tokenResponse }

    val accessToken = OAuth2Client(tokenLoader).getAccessToken(ClientAuthTransport.HEADER)
    return Pair(accessToken, tokenLoader)
}