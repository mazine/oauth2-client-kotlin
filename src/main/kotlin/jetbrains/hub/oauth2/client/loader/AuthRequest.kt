package jetbrains.hub.oauth2.client.loader

import jetbrains.hub.oauth2.client.BasicAuth
import java.net.URI

enum class GrantType(val value: String) {
    CLIENT_CREDENTIALS("client_credentials"),
    PASSWORD("password"),
    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token")
}

enum class ResponseType(val value: String) {
    TOKEN("token"),
    CODE("code")
}

enum class ClientAuthTransport() { HEADER, FORM }

enum class PromptApproval(val value: String) {
    FORCE("force"),
    AUTO("auto")
}

enum class RequestCredentials(val value: String?) {
    /**
     * ```
     * when
     *   already logged in -> return logged in user
     *   else -> show login form
     * ```
     */
    DEFAULT(null),
    /**
     * Log out currently logged in user, and show login form
     */
    REQUIRED("required"),
    /**
     * ```
     * when
     *   already logged in -> return logged in user
     *   guest is banned -> show login form
     *   else -> return guest
     * ```
     */
    SKIP("skip"),
    /**
     * ```
     * when
     *   already logged in -> return logged in user
     *   guest is banned -> return nothing
     *   else -> return guest
     * ```
     */
    SILENT("silent"),
    /**
     * Log out currently logged in user, and return back
     */
    SILENT_LOGOUT("silent_logout")
}

enum class AccessType(val value: String?) {
    /**
     * Don't request refresh token
     */
    ONLINE(null),
    /**
     * Request refresh token
     */
    OFFLINE("offline")
}


internal class AuthRequest(val uri: URI, var clientID: String) {
    var authResponseType: ResponseType? = null
    var state: String? = null
    var redirectURI: URI? = null
    var message: String? = null
    var requestRefreshToken: Boolean = false
    var prompt: PromptApproval? = null
    var requestCredentials: RequestCredentials = RequestCredentials.DEFAULT
    var scope: List<String>? = null

    val queryParameters: Sequence<Pair<String, String?>>
        get() = sequenceOf(
                "response_type" to authResponseType?.value,
                "client_id" to clientID,
                "scope" to scope?.joinToString(" "),
                "message" to message,
                "redirect_uri" to redirectURI?.toASCIIString(),
                "state" to state,
                "approval_prompt" to prompt?.value?.toLowerCase(),
                "request_credentials" to requestCredentials.value?.toLowerCase(),
                "access_type" to if (requestRefreshToken) {
                    AccessType.OFFLINE
                } else {
                    AccessType.ONLINE
                }.value
        )
}

internal class TokenRequest(val uri: URI, val clientID: String, val clientSecret: String) {
    var grantType: GrantType? = null
    var authTransport: ClientAuthTransport = ClientAuthTransport.HEADER
    var username: String? = null
    var password: String? = null
    var code: String? = null
    var refreshToken: String? = null
    var redirectURI: URI? = null
    var requestRefreshToken: Boolean = false
    var scope: List<String>? = null


    val queryParameters: Sequence<Pair<String, String?>>
        get() = sequenceOf(
                "grant_type" to grantType?.value,
                "username" to username,
                "password" to password,
                "code" to code,
                "scope" to scope?.joinToString(" "),
                "redirect_uri" to redirectURI?.toASCIIString(),
                "access_type" to if (requestRefreshToken) {
                    AccessType.OFFLINE
                } else {
                    AccessType.ONLINE
                }.value,
                "refresh_token" to refreshToken
        )

    val headers: Sequence<Pair<String, String>>
        get() {
            val acceptHeader = sequenceOf("Accept" to "application/json")
            return if (authTransport == ClientAuthTransport.HEADER) {
                val clientID = clientID
                val basicAuthHeader = BasicAuth(clientID, clientSecret).header
                acceptHeader + sequenceOf(basicAuthHeader.name to basicAuthHeader.value)
            } else {
                acceptHeader
            }
        }

    val formParameters: Sequence<Pair<String, String>>?
        get() {
            return if (authTransport == ClientAuthTransport.FORM) {
                sequenceOf(
                        "client_id" to clientID,
                        "client_secret" to clientSecret)

            } else {
                null
            }
        }

}