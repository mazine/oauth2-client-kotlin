package jetbrains.hub.oauth2.client

import java.util.*

class RefreshToken(accessToken: String,
                   expiresAt: Calendar,
                   scope: List<String>,
                   val refreshTokenID: String) :
        AccessToken(accessToken, expiresAt, scope)
