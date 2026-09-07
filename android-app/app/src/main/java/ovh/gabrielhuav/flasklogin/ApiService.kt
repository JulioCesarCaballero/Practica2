package ovh.gabrielhuav.flasklogin

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("register")
    suspend fun register(@Body body: RegisterRequest): Response<MessageResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("tasks")
    suspend fun getTasks(@Header("Authorization") token: String): Response<List<Task>>

    @POST("tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Body body: TaskRequest
    ): Response<Task>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: TaskRequest
    ): Response<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}