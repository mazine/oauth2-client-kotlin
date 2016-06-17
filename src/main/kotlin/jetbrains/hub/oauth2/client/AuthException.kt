package jetbrains.hub.oauth2.client

class AuthException(val error: String, val errorDescription: String?) :
        RuntimeException(errorDescription ?: "Authentication error: $error")