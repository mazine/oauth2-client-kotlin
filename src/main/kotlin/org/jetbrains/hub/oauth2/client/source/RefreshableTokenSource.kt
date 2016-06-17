package org.jetbrains.hub.oauth2.client.source

import java.util.concurrent.atomic.AtomicReference

abstract class RefreshableTokenSource() : TokenSource {
    private val cachedToken = AtomicReference<org.jetbrains.hub.oauth2.client.AccessToken>()

    override val accessToken: org.jetbrains.hub.oauth2.client.AccessToken
        get() {
            val current: org.jetbrains.hub.oauth2.client.AccessToken? = cachedToken.get()
            val newToken = when {
                current == null -> loadToken()
                current.isExpired -> loadToken()
                else -> current
            }
            cachedToken.compareAndSet(current, newToken)
            return newToken
        }

    protected abstract fun loadToken(): org.jetbrains.hub.oauth2.client.AccessToken
}
