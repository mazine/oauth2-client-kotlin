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


### Maven and Gradle

**<span style="color:red">TBD: NOT PUBLISHED YET TO MAVEN</span>**

You can download it from the [JetBrains Artifactory](http://repository.jetbrains.com).

To use it in Maven insert the following in your pom.xml file:
``` xml
 <dependency>
    <groupId>org.jetbrains.hub</groupId>
    <artifactId>oauth2-client</artifactId>
    <version>$version</version>
 </dependency>

 <repositories>
    <repository>
      <id>jebrains-all</id>
      <url>http://repository.jetbrains.com/all</url>
    </repository>
  </repositories>
```

For Gradle:
``` groovy
repositories {
    maven { url "http://repository.jetbrains.com/all" }
}

dependencies {
    testCompile 'org.jetbrains.hub:oauth2-client:$version'
}
```

## Usage

### Code Flow

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
4. User identifies herself at the OAuth 2.0 server (e.g. by entering username and password).
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
val targetURI = OAuth2Client().codeFlowURI(
        authEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/auth"),
        clientID = "1234-3213-3123",
        redirectURI = URI("https://localhost:8080/auth"),
        scope = listOf("0-0-0-0-0", clientID),
        state = "some-unique-state-id")
```

**Exchange code**
```
val accessToken = OAuth2Client().codeFlow(
        tokenEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/token"),
        code = "sOMec0de",
        redirectURI = URI("https://localhost:8080/auth"),
        clientID = "1234-3213-3123",
        clientSecret = "sGUl4x")

do {
    // Make various calls using accessToken.header
} while (!accessToken.isExpired)
```

### Client Flow

**Use it if**
- Your application accesses resources on behalf of itself.
- The `Client ID`, `Client Secret` and any access token issued to your application are stored confident.

The library allows to create a `RefreshableTokenSource` for this flow. It is an object that retrieves and
caches an `Access Token`, and renews the `Access Token` when it expires.

```
val tokenSource = OAuth2Client().clientFlow(
        tokenEndpoint = URI("https://hub.jetbrains.com/api/rest/oauth2/token"),
        clientID = "1234-3213-3123",
        clientSecret = "sGUl4x",
        scope = listOf("0-0-0-0-0", clientID))

do {
    // Make various calls using tokenSource.accessToken.header
} while (true)
```