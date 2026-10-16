package online.codyuhi.copypastetool.data.api

import online.codyuhi.copypastetool.data.model.AuthResponse
import online.codyuhi.copypastetool.data.model.GenericResponse
import online.codyuhi.copypastetool.data.model.LoginRequest
import online.codyuhi.copypastetool.data.model.RegisterRequest
import online.codyuhi.copypastetool.data.model.User
import online.codyuhi.copypastetool.data.model.UserDataPayload
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface CopyPasteApi {
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") auth: String): Response<GenericResponse>

    @GET("api/auth/me")
    suspend fun getMe(@Header("Authorization") auth: String): Response<Map<String, User>>

    @GET("api/data")
    suspend fun getData(@Header("Authorization") auth: String): Response<UserDataPayload>

    @PUT("api/data")
    suspend fun putData(
        @Header("Authorization") auth: String,
        @Body req: UserDataPayload
    ): Response<GenericResponse>

    @GET("healthz")
    suspend fun getHealth(): Response<Map<String, String>>
}
