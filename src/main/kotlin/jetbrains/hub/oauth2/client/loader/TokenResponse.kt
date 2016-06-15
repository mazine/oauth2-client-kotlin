package jetbrains.hub.oauth2.client.loader

import java.util.*

sealed class TokenResponse() {
    class Success(
            val accessToken: String,
            val refreshToken: String?,
            val expiresIn: Int,
            val requestTime: Calendar,
            val scope: List<String>) : TokenResponse() {

        val expiresAt by lazy {
            Calendar.getInstance().apply {
                timeInMillis = requestTime.timeInMillis + expiresIn * 1000
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false

            if (accessToken != other.accessToken) return false
            if (refreshToken != other.refreshToken) return false

            return true
        }

        override fun hashCode(): Int {
            var result = accessToken.hashCode()
            result = 31 * result + (refreshToken?.hashCode() ?: 0)
            return result
        }
    }

    class Error(var error: String, var description: String? = null) : TokenResponse()
}