package ovh.gabrielhuav.flasklogin

object SessionManager {
    var token: String? = null

    fun bearerToken(): String = "Bearer ${token ?: ""}"

    fun clear() {
        token = null
    }
}