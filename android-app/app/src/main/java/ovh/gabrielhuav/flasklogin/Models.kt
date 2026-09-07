package ovh.gabrielhuav.flasklogin

data class RegisterRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val status: String,
    val message: String,
    val access_token: String?,
    val user_id: Int?,
    val username: String?
)

data class MessageResponse(val message: String)

data class Task(val id: Int, val title: String, val done: Boolean)
data class TaskRequest(val title: String? = null, val done: Boolean? = null)