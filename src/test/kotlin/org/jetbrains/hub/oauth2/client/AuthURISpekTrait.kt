package org.jetbrains.hub.oauth2.client

import org.jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import org.jetbrains.hub.oauth2.client.loader.TokenResponse
import org.jetbrains.spek.api.DescribeBody
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull


fun DescribeBody.itShouldBeValidRefreshTokenSource(
        expectedTokenEndpoint: URI,
        clientID: String, clientSecret: String,
        expectedFormParameters: Map<String, String>,
        getRefreshToken: OAuth2Client.(ClientAuthTransport) -> String) {
    it("should parse refresh token correctly") {
        val (refreshToken, tokenLoader) = createTokenSourceAndLoader(getRefreshToken, TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf("0-0-0-0-0"))
        )
        assertEquals("refresh-token", refreshToken)

        assertEquals(1, tokenLoader.loadRecords.size)
    }

    it("should call correct token endpoint") {
        requestToken({ getRefreshToken(ClientAuthTransport.HEADER) }) {
            assertEquals(expectedTokenEndpoint, uri)
        }
    }

    it("should request token with correct form parameters") {
        requestToken({ getRefreshToken(ClientAuthTransport.HEADER) }) {
            assertEquals(expectedFormParameters, this.formParameters)
        }
    }

    it("should pass credentials as header parameters if required") {
        requestToken({ getRefreshToken(ClientAuthTransport.HEADER) }) {
            assertEquals("Basic ${Base64.encode("$clientID:$clientSecret".toByteArray())}", headers["Authorization"])
            assertNull(formParameters["client_id"])
            assertNull(formParameters["client_secret"])
        }
    }

    it("should pass credentials as form parameters if required") {
        requestToken({ getRefreshToken(ClientAuthTransport.FORM) }) {
            assertNull(headers["Authorization"])
            assertEquals(clientID, formParameters["client_id"])
            assertEquals(clientSecret, formParameters["client_secret"])
        }
    }

    it("shouldn't access server unless token is requested") {
        val tokenLoader = createTokenSourceAndLoader(getRefreshToken).second
        assertEquals(1, tokenLoader.loadRecords.size)
    }
}

private fun requestToken(
        getRefreshToken: OAuth2Client.() -> String,
        assertTokenRequest: MockTokenLoader.Request.() -> Unit) {
    OAuth2Client(MockTokenLoader {
        assertTokenRequest()
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf("0-0-0-0-0"))

    }).getRefreshToken()
}

private fun createTokenSourceAndLoader(
        getRefreshToken: OAuth2Client.(ClientAuthTransport) -> String,
        tokenResponse: TokenResponse = TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 3600,
                requestTime = Calendar.getInstance(),
                scope = listOf())): Pair<String, MockTokenLoader> {
    val tokenLoader = MockTokenLoader { tokenResponse }

    val flow = OAuth2Client(tokenLoader).getRefreshToken(ClientAuthTransport.HEADER)
    return Pair(flow, tokenLoader)
}