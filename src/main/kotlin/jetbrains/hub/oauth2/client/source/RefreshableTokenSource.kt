package jetbrains.hub.oauth2.client.source

import jetbrains.hub.oauth2.client.AccessToken
import java.util.concurrent.atomic.AtomicReference

abstract class RefreshableTokenSource(): TokenSource {
    private val cachedToken = AtomicReference<AccessToken>()

    override val accessToken: AccessToken
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
