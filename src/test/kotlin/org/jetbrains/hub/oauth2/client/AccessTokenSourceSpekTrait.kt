package org.jetbrains.hub.oauth2.client

import org.jetbrains.hub.oauth2.client.loader.ClientAuthTransport
import org.jetbrains.hub.oauth2.client.loader.TokenResponse
import org.jetbrains.spek.api.DescribeBody
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


fun DescribeBody.itShouldBeRefreshableTokenSource(
        getFlow: OAuth2Client.(ClientAuthTransport) -> AccessTokenSource) {
    it("shouldn't access server unless token is requested") {
        val tokenLoader = createTokenSourceAndLoader(getFlow).second
        assertTrue(tokenLoader.loadRecords.isEmpty())
    }

    it("should cache token unless it is expired") {
        val (source, tokenLoader) = createTokenSourceAndLoader(getFlow)
        source.accessToken
        source.accessToken
        assertEquals(1, tokenLoader.loadRecords.size)
    }

    it("should refresh token when it is expired") {
        val (source, tokenLoader) = createTokenSourceAndLoader(getFlow, TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf())
        )

        source.accessToken
        source.accessToken

        assertEquals(2, tokenLoader.loadRecords.size)
    }
}


private fun createTokenSourceAndLoader(
        getAccessTokenSource: OAuth2Client.(ClientAuthTransport) -> AccessTokenSource,
        tokenResponse: TokenResponse = TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = Calendar.getInstance(),
                scope = listOf())): Pair<AccessTokenSource, MockTokenLoader> {
    val tokenLoader = MockTokenLoader { tokenResponse }

    val tokenSource = OAuth2Client(tokenLoader).getAccessTokenSource(ClientAuthTransport.HEADER)
    return Pair(tokenSource, tokenLoader)
}