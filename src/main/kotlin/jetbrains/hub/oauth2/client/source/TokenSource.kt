package jetbrains.hub.oauth2.client.source

import jetbrains.hub.oauth2.client.AccessToken

interface TokenSource {
    val accessToken: AccessToken
}