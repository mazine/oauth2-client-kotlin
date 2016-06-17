package org.jetbrains.hub.oauth2.client

/**
 * Data class for authorization header name and value
 */
data class AuthHeader(val value: String) {
    val name: String
        get() = "Authorization"
}