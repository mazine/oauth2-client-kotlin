package org.jetbrains.hub.oauth2.client

import org.jetbrains.hub.oauth2.client.loader.TokenLoader
import org.jetbrains.hub.oauth2.client.loader.TokenResponse
import java.net.URI
import java.net.URLEncoder
import java.util.*

open class MockTokenLoader(var onTokenRequest: Request.() -> TokenResponse) : TokenLoader {
    val loadRecords = ArrayList<Request>()

    override fun load(uri: URI, headers: Map<String, String>, formParameters: Map<String, String>): TokenResponse {
        val request = Request(uri, headers, formParameters)
        loadRecords.add(request)
        return request.onTokenRequest()
    }

    override fun authURI(uri: URI, queryParameters: Map<String, String>): URI {
        return URI.create("${uri.toASCIIString()}?${queryParameters.map {
            val eName = URLEncoder.encode(it.key, "UTF-8")
            val eValue = URLEncoder.encode(it.value, "UTF-8")
            "$eName=$eValue"
        }.joinToString("&")}")
    }

    class Request(val uri: URI, val headers: Map<String, String>, val formParameters: Map<String, String>)
}