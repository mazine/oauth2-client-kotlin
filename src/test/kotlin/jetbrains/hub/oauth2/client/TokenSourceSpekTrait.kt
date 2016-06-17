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
        expectedTokenEndpoint: URI,
        clientID: String, clientSecret: String,
        expectedFormParameters: Map<String, String>,
        getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
    it("should parse access token correctly") {
        val (source, tokenLoader) = createTokenSourceAndLoader(getFlow, TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf("0-0-0-0-0"))
        )
        val accessToken = source.accessToken
        assertEquals("access-token", accessToken.accessToken)
        assertEquals("2016-06-16 13:00:00", accessToken.expiresAt.asString())
        assertEquals(listOf("0-0-0-0-0"), accessToken.scope)

        assertEquals(1, tokenLoader.loadRecords.size)
    }

    it("should call correct token endpoint") {
        requestToken({ getFlow(ClientAuthTransport.HEADER) }) {
            assertEquals(expectedTokenEndpoint, uri)
        }
    }

    it("should request token with correct form parameters") {
        requestToken({ getFlow(ClientAuthTransport.HEADER) }) {
            assertEquals(expectedFormParameters, this.formParameters)
        }
    }

    it("should pass credentials as header parameters if required") {
        requestToken({ getFlow(ClientAuthTransport.HEADER) }) {
            assertEquals("Basic ${Base64.encode("$clientID:$clientSecret".toByteArray())}", headers["Authorization"])
            assertNull(formParameters["client_id"])
            assertNull(formParameters["client_secret"])
        }
    }

    it("should pass credentials as form parameters if required") {
        requestToken({ getFlow(ClientAuthTransport.FORM) }) {
            assertNull(headers["Authorization"])
            assertEquals(clientID, formParameters["client_id"])
            assertEquals(clientSecret, formParameters["client_secret"])
        }
    }

    it("shouldn't access server unless token is requested") {
        val tokenLoader = createTokenSourceAndLoader(getFlow).second
        assertTrue(tokenLoader.loadRecords.isEmpty())
    }
}

fun DescribeBody.itShouldBeRefreshableTokenSource(
        getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource) {
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

private fun requestToken(
        getFlow: OAuth2Client.() -> TokenSource,
        assertTokenRequest: MockTokenLoader.Request.() -> Unit) {
    OAuth2Client(MockTokenLoader {
        assertTokenRequest()
        TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                expiresIn = 3600,
                requestTime = "2016-06-16 12:00:00".toCalendar(),
                scope = listOf("0-0-0-0-0"))

    }).getFlow().accessToken
}

private fun createTokenSourceAndLoader(
        getFlow: OAuth2Client.(ClientAuthTransport) -> TokenSource,
        tokenResponse: TokenResponse = TokenResponse.Success(
                accessToken = "access-token",
                refreshToken = null,
                expiresIn = 3600,
                requestTime = Calendar.getInstance(),
                scope = listOf())): Pair<TokenSource, MockTokenLoader> {
    val tokenLoader = MockTokenLoader { tokenResponse }

    val flow = OAuth2Client(tokenLoader).getFlow(ClientAuthTransport.HEADER)
    return Pair(flow, tokenLoader)
}