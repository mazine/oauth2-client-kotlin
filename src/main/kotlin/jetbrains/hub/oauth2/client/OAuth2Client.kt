package jetbrains.hub.oauth2.client

import jetbrains.hub.oauth2.client.loader.*
import jetbrains.hub.oauth2.client.source.RefreshableTokenSource
import java.net.URI

class OAuth2Client(val tokenLoader: TokenLoader) {
    fun clientFlow(
            tokenEndpoint: URI,
            clientID: String,
            clientSecret: String,
            scope: List<String>,
            authTransport: ClientAuthTransport = ClientAuthTransport.HEADER): RefreshableTokenSource {
        return refreshableTokenSource(TokenRequest(tokenEndpoint, clientID, clientSecret).apply {
            this.grantType = GrantType.CLIENT_CREDENTIALS
            this.scope = scope
            this.authTransport = authTransport
        })
    }

    fun resourceOwnerFlow(
            tokenEndpoint: URI,
            username: String, password: String,
            clientID: String, clientSecret: String,
            scope: List<String>,
            authTransport: ClientAuthTransport = ClientAuthTransport.HEADER): RefreshableTokenSource {
        return refreshableTokenSource(resourceOwnerTokenRequest(
                tokenEndpoint, username, password,
                clientID, clientSecret,
                scope, authTransport, requestRefreshToken = false))
    }

    fun resourceOwnerRefreshToken(
            tokenEndpoint: URI,
            username: String, password: String,
            clientID: String, clientSecret: String,
            scope: List<String>,
            authTransport: ClientAuthTransport = ClientAuthTransport.HEADER): String {
        return tokenLoader.
                load(resourceOwnerTokenRequest(
                        tokenEndpoint, username, password,
                        clientID, clientSecret,
                        scope, authTransport, requestRefreshToken = true)).
                asRefreshToken()
    }

    private fun resourceOwnerTokenRequest(
            tokenEndpoint: URI,
            username: String, password: String,
            clientID: String, clientSecret: String,
            scope: List<String>,
            authTransport: ClientAuthTransport = ClientAuthTransport.HEADER,
            requestRefreshToken: Boolean = false): TokenRequest {
        return TokenRequest(tokenEndpoint, clientID, clientSecret).apply {
            this.grantType = GrantType.PASSWORD
            this.scope = scope
            this.authTransport = authTransport
            this.username = username
            this.password = password
            this.requestRefreshToken = requestRefreshToken
        }
    }


    fun codeFlowURI(
            authEndpoint: URI,
            redirectURI: URI,
            clientID: String,
            scope: List<String>,
            state: String,
            requestRefreshToken: Boolean = false,
            message: String? = null,
            prompt: PromptApproval? = null,
            requestCredentials: RequestCredentials = RequestCredentials.DEFAULT): URI {

        return tokenLoader.authURI(AuthRequest(authEndpoint, clientID).apply {
            this.authResponseType = ResponseType.CODE
            this.state = state
            this.redirectURI = redirectURI
            this.scope = scope
            this.message = message
            this.requestRefreshToken = requestRefreshToken
            this.prompt = prompt
            this.requestCredentials = requestCredentials
        })
    }

    fun codeFlow(
            tokenEndpoint: URI,
            code: String,
            redirectURI: URI,
            clientID: String,
            clientSecret: String,
            authTransport: ClientAuthTransport = ClientAuthTransport.HEADER): RefreshableTokenSource {
        return refreshableTokenSource(codeFlowTokenRequest(
                tokenEndpoint, code, redirectURI,
                clientID, clientSecret, authTransport))
    }

    fun codeRefreshToken(
            tokenEndpoint: URI,
            code: String,
            redirectURI: URI,
            clientID: String,
            clientSecret: String,
            authTransport: ClientAuthTransport = ClientAuthTransport.HEADER): String {
        return tokenLoader.
                load(codeFlowTokenRequest(
                        tokenEndpoint, code, redirectURI,
                        clientID, clientSecret, authTransport)).
                asRefreshToken()
    }

    private fun codeFlowTokenRequest(
            tokenEndpoint: URI,
            code: String,
            redirectURI: URI,
            clientID: String,
            clientSecret: String,
            authTransport: ClientAuthTransport = ClientAuthTransport.HEADER): TokenRequest {
        return TokenRequest(tokenEndpoint, clientID, clientSecret).apply {
            this.grantType = GrantType.AUTHORIZATION_CODE
            this.authTransport = authTransport
            this.redirectURI = redirectURI
            this.code = code
        }
    }

    fun implicitFlowURI(
            authEndpoint: URI,
            redirectURI: URI,
            clientID: String,
            scope: List<String>,
            state: String,
            message: String? = null,
            prompt: PromptApproval? = null,
            requestCredentials: RequestCredentials = RequestCredentials.DEFAULT): URI {

        return tokenLoader.authURI(AuthRequest(authEndpoint, clientID).apply {
            this.authResponseType = ResponseType.TOKEN
            this.state = state
            this.redirectURI = redirectURI
            this.scope = scope
            this.message = message
            this.prompt = prompt
            this.requestCredentials = requestCredentials
        })
    }

    fun refreshTokenFlow(tokenEndpoint: URI,
                         refreshToken: String,
                         clientID: String,
                         clientSecret: String,
                         scope: List<String>,
                         authTransport: ClientAuthTransport = ClientAuthTransport.HEADER): RefreshableTokenSource {
        return refreshableTokenSource(TokenRequest(tokenEndpoint, clientID, clientSecret).apply {
            this.grantType = GrantType.REFRESH_TOKEN
            this.refreshToken = refreshToken
            this.scope = scope
            this.authTransport = authTransport
        })
    }

    private fun refreshableTokenSource(tokenRequest: TokenRequest): RefreshableTokenSource {
        return object : RefreshableTokenSource() {
            override fun loadToken(): AccessToken {
                val response = tokenLoader.load(tokenRequest)
                return response.asAccessToken()
            }
        }
    }

    private fun TokenLoader.authURI(authRequest: AuthRequest): URI {
        return authURI(authRequest.uri, authRequest.queryParameters.mapNotNull())
    }

    private fun Sequence<Pair<String, String?>>.mapNotNull(): Map<String, String> {
        return mapNotNull {
            it.second?.let { value -> it.first to value }
        }.toMap()
    }

    private fun TokenLoader.load(tokenRequest: TokenRequest): TokenResponse {
        return load(tokenRequest.uri,
                tokenRequest.queryParameters.mapNotNull(),
                tokenRequest.headers.mapNotNull(),
                tokenRequest.formParameters?.mapNotNull())
    }

    private fun TokenResponse.asAccessToken() = when (this) {
        is TokenResponse.Success ->
            AccessToken(accessToken, expiresAt, scope)
        is TokenResponse.Error ->
            throw AuthException(error, description)
    }

    private fun TokenResponse.asRefreshToken() = when (this) {
        is TokenResponse.Success ->
            refreshToken ?: throw AuthException("refresh_failed", "Failed to request refresh token")
        is TokenResponse.Error ->
            throw AuthException(error, description)
    }

}