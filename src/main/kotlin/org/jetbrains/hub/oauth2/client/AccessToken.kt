package org.jetbrains.hub.oauth2.client


open class AccessToken(
        val accessToken: String,
        val expiresAt: java.util.Calendar,
        val scope: List<String>) {

    val isExpired: Boolean
        get() = java.util.Calendar.getInstance().after(expiresAt)

    val header: AuthHeader
        get() = AuthHeader("Bearer $accessToken")
}
