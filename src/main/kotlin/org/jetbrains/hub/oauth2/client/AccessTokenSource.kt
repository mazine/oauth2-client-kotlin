package org.jetbrains.hub.oauth2.client

import java.util.concurrent.atomic.AtomicReference

abstract class AccessTokenSource() {
    private val cachedToken = AtomicReference<AccessToken>()

    val accessToken: AccessToken
        get() {
            val current: AccessToken? = cachedToken.get()
            val newToken = when {
                current == null -> loadToken()
                current.isExpired -> loadToken()
                else -> current
            }
            cachedToken.compareAndSet(current, newToken)
            return newToken
        }

    protected abstract fun loadToken(): AccessToken
}
