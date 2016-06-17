package org.jetbrains.hub.oauth2.client

import java.util.*


open class AccessToken(
        val accessToken: String,
        val expiresAt: Calendar,
        val scope: List<String>) {

    val isExpired: Boolean
        get() = Calendar.getInstance().after(expiresAt)

    val header: AuthHeader
        get() = AuthHeader("Bearer $accessToken")
}
