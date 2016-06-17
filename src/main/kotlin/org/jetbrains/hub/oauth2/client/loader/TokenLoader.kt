package org.jetbrains.hub.oauth2.client.loader

import java.net.URI

interface TokenLoader {
    fun load(uri: URI,
             headers: Map<String, String>,
             formParameters: Map<String, String>): TokenResponse

    fun authURI(uri: URI, queryParameters: Map<String, String>): URI
}