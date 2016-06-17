package org.jetbrains.hub.oauth2.client.source

interface TokenSource {
    val accessToken: org.jetbrains.hub.oauth2.client.AccessToken
}