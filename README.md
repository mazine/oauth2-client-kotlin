## Overview
Tiny OAuth 2.0 client library in Kotlin that supports [JetBrains Hub](http://jetbrains.com/hub) authorization
features.

In the most cases to enable authorization via OAuth 2.0 server you have to know
`Client ID` and `Client Secret` that you get when you register your application at the
OAuth 2.0 server. During the registration you often provide a `Redirect URI` to the OAuth 2.0 server.
This URI is actually a URI in your application that handles responses from the OAuth 2.0 server.

To perform operations authorized by the OAuth 2.0 server, your application requires an `Access Token`. There
are several ways (so called flows) for your applications to get it. The flow you will use depends on the
environment your application runs in.

### Gradle

``` groovy
repositories {
    jcenter()
    maven { url "https://jitpack.io" }
}
dependencies {
     compile 'com.github.mazine:oauth2-client-kotlin:1.0'
}
```

## Usage

### Code Flow <a id="code-flow"></a>

**Use it if**
- Your application is running on a web server.
- Your application builds HTML responses on the server side.
- The `Client ID`, `Client Secret` and any access token issued to your application are stored on the web server
  and are not accessible by the end-user.

**How It Works?**
1. User tries to access your application via browser and your application finds out that it doesn't have yet an `Access
   Token` for the user.
2. Your application saves the information about the URI, user tried to access, under the unique identifier `state`.
3. Your application redirects the user to the OAuth 2.0 server passing the `state` as one of the parameters.
4. User identifies herself at the OAuth 2.0 server (e.g. by entering her username and password).
5. OAuth 2.0 server redirects the user back to your application (to the `Redirect URI` to be precise) with two query
   parameters: `state` and `code`.
6. Your application makes a server to server HTTP call to the OAuth 2.0 server exchanging the `code` for the `Access
   Token`. To identify itself your application passes with the call its `Client ID` and `Client Secret`.
7. Your application using the `state` restores the URI originally requested by the user, and redirects her there.

For further details check [OAuth 2.0 Spec](https://tools.ietf.org/html/rfc6749#section-4.1)
or [Hub Docs](https://www.jetbrains.com/help/hub/2.0/Authorization-Code.html).

The library actually helps to build the URI to redirect the user on the step 3, and to exchange the `code` for
the `Access Token` on the step 6.

**Build URI**
```
val targetURI = oauth2Client().codeFlowURI(
        authEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/auth"),
        clientID = "1234-3213-3123",
        redirectURI = URI("https://localhost:8080/auth"),
        scope = listOf("0-0-0-0-0", clientID),
        state = "some-unique-state-id")
```

**Exchange code**
```
val accessToken = oauth2Client().codeFlow(
        tokenEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/token"),
        code = "sOMec0de",
        redirectURI = URI("https://localhost:8080/auth"),
        clientID = "1234-3213-3123",
        clientSecret = "sGUl4x")

do {
    // Make various calls using accessToken.header
} while (!accessToken.isExpired)
```

### Client Flow <a id="client-flow"></a>

**Use it if**
- Your application accesses resources on behalf of itself.
- The `Client ID`, `Client Secret` and any access token issued to your application are stored confident.

For further details check [OAuth 2.0 Spec](http://tools.ietf.org/html/rfc6749#section-4.4)
or [Hub Docs](https://www.jetbrains.com/help/hub/2.0/Client-Credentials.html).

The library allows to create an `AccessTokenSource` for this flow. It is an object that retrieves and
caches an `Access Token`, and renews the `Access Token` when it expires.

```
val tokenSource = oauth2Client().clientFlow(
        tokenEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/token"),
        clientID = "1234-3213-3123",
        clientSecret = "sGUl4x",
        scope = listOf("0-0-0-0-0", clientID))

do {
    // Make various calls using tokenSource.accessToken.header
} while (true)
```

### Resource Owner Flow <a id="resource-owner-flow"></a>

**Use it if**

Your application knows user credentials and accesses resources on behalf of a user. For example, your application is
the device operating system or a highly privileged application.

For further details check [OAuth 2.0 Spec](http://tools.ietf.org/html/rfc6749#section-4.3)
or [Hub Docs](https://www.jetbrains.com/help/hub/2.0/Resource-Owner-Password-Credentials.html).

The library allows to create an `AccessTokenSource` for this flow.

```
val tokenSource = oauth2Client().resourceOwnerFlow(
        tokenEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/token"),
        username = "john.doe",
        password = "p@$Sw0rd",
        clientID = "1234-3213-3123",
        clientSecret = "sGUl4x",
        scope = listOf("0-0-0-0-0", clientID))

do {
    // Make various calls using tokenSource.accessToken.header
} while (true)
```

### Implicit Flow <a id="implicit-flow"></a>

**Use it if**

Your application is public. Typically a JavaScript code in a browser.

**How It Works?**
1. User downloads your JavaScript application into her browser. To access resources via REST API your application
   needs an `Access Token`.
2. Your application finds out that it has no `Access Token` yet.
3. Your application saves the information about the URI, user tried to access, under the unique identifier `state`
   (e.g. in a local storage of the browser).
4. Your application redirects the user to the OAuth 2.0 server passing the `state` as one of the parameters.
5. User identifies herself at the OAuth 2.0 server (e.g. by entering her username and password).
6. OAuth 2.0 server redirects the user back to your application (to the `Redirect URI` to be precise) with an
   `Access Token` in parameters after ‘`#`’. The trick here is that browser sends nothing after ‘`#`’ in URL to
   the server, but the part after ‘`#`’ is accessible for your JavaScript application. So the `Access Token` never
   leaves user's browser.

For further details check [OAuth 2.0 Spec](http://tools.ietf.org/html/rfc6749#section-4.2)
or [Hub Docs](https://www.jetbrains.com/help/hub/2.0/Implicit.html).

The library only helps to build the URI to redirect the user on the step 4.

```
val targetURI = oauth2Client().implicitFlowURI(
        authEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/auth"),
        clientID = "1234-3213-3123",
        redirectURI = URI("https://localhost:8080/auth"),
        scope = listOf("0-0-0-0-0", clientID),
        state = "some-unique-state-id")
```

### Refresh Token <a id="refresh-token"></a>

**Use it if**

Your application is a desktop or mobile application that wants to access resources on behalf of user,
when the user is offline.

For further details check [OAuth 2.0 Spec](https://tools.ietf.org/html/rfc6749#section-4.1)
or [Hub Docs](https://www.jetbrains.com/help/hub/2.0/Refresh-Token.html).

Your application can obtain a `Refresh Token` as a part of [code](#code-flow) or [resource owner](#resource-owner-flow)
flows.

**Obtain `Refresh Token` from code flow**

When redirect to OAuth 2.0 server, request refreshToken
```
oauth2Client().codeFlowURI(
        authEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/auth"),
        clientID = "1234-3213-3123",
        redirectURI = URI("https://localhost:8080/auth"),
        scope = listOf("0-0-0-0-0", clientID),
        state = "some-unique-state-id",
        requestRefreshToken = true)
```

When user returns with a `code`, use the `code` to obtain refresh token
```
val refreshToken = oauth2Client().codeRefreshToken(
        tokenEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/token"),
        code = "sOMec0de",
        redirectURI = URI("https://localhost:8080/auth"),
        clientID = "1234-3213-3123",
        clientSecret = "sGUl4x")
```

**Obtain `Refresh Token` from resource owner flow**
```
val refreshToken = oauth2Client().resourceOwnerRefreshToken(
        tokenEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/token"),
        username = "john.doe",
        password = "p@$Sw0rd",
        clientID = "1234-3213-3123",
        clientSecret = "sGUl4x",
        scope = listOf("0-0-0-0-0", clientID))
```

**Use `Refresh Token` to get `AccessTokenSource`**
```
val tokenSource = oauth2Client().refreshTokenFlow(
        tokenEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/token"),
        refreshToken = "that-refresh-token",
        clientID = "1234-3213-3123",
        clientSecret = "sGUl4x",
        scope = listOf("0-0-0-0-0", clientID))

do {
    // Make various calls using tokenSource.accessToken.header
} while (true)
```