package jetbrains.hub.oauth2.client.source

import jetbrains.hub.oauth2.client.AccessToken

class OneTimeTokenSource(override val accessToken: AccessToken) : TokenSource