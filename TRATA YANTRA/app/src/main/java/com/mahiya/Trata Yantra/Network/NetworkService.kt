package com.mahiya.safegas.network

import android.util.Log
import com.mahiya.safegas.data.GasReadingItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// Data classes for Django API
data class LoginRequest(
    val email: String,
    val password: String
)

data class CsrfResponse(
    val csrfToken: String
)



data class ApiResponse<T>(
    val success: Any? = null,  // Can handle both Int and Boolean from Django
    val message: String? = null,
    val data: T? = null,
    val token: String? = null,
    val user: User? = null,
    val error: String? = null,
    // Add Django JWT-specific fields
    val access: String? = null,        // Django access token
    val refresh: String? = null,       // Django refresh token
    val email: String? = null,         // Django returns email directly
    val device_unique_code: String? = null
)




data class User(
    val id: Int,
    val username: String,
    val email: String
)

data class DashboardData(
    val status_data: List<StatusData>,
    val recent_activity: List<ActivityItem>
)

data class StatusData(
    val title: String,
    val value: String,
    val icon: String,
    val color: String,
    val is_healthy: Int? = 0
)

data class ActivityItem(
    val icon: String,
    val title: String,
    val time: String,
    val color: String
)

data class GasLeakData(
    val status: String,
    val gas_level: Float,
    val sensor_count: Int,
    val last_scan: String
)

data class GasLevelData(
    val current_level: Float,
    val estimated_hours: Float,
    val flow_rate: Float,
    val pressure: Float,
    val recent_readings: List<GasReadingItem>
)

data class GasReading(
    val time: String,
    val level: Float,
    val status: String
)

data class PipelineHealthData(
    val overall_health: Float,
    val sections: List<PipelineSectionItem>,
    val maintenance_schedule: List<MaintenanceItem>
)

data class PipelineSectionItem(
    val id: Int,
    val section_name: String,
    val health_percentage: Float,
    val status: String,
    val last_inspection: String?
)

data class MaintenanceItem(
    val id: Int,
    val title: String,
    val description: String,
    val scheduled_date: String,
    val priority: String
)

data class RegulatorData(
    val regulator_id: String,
    val is_on: Boolean,
    val auto_mode: Boolean,
    val current_pressure: Float,
    val flow_rate: Float,
    val temperature: Float
)

data class EmergencyContactData(
    val id: Int,
    val name: String,
    val phone_number: String,
    val relationship: String,
    val is_primary: Boolean
)

data class EmergencyContactRequest(
    val name: String,
    val phone_number: String,
    val relationship: String,
    val is_primary: Boolean = false
)

data class ProfileData(
    val username: String,
    val email: String,
    val phone_number: String?,
    val profile_image: String?,
    val device_unique_code: String
)

data class ProfileImageResponse(
    val image_url: String
)

interface ApiService {
    @POST("auth/register/")
    suspend fun signup(@Body request: SignupRequest): ApiResponse<Any>

    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): ApiResponse<Any>

    @GET("home/dashboard/")
    suspend fun getDashboardData(@Header("Authorization") token: String): ApiResponse<DashboardData>

    @GET("home/gas-leak/")
    suspend fun getGasLeakData(@Header("Authorization") token: String): ApiResponse<Any>

    @GET("auth/csrf/")
    suspend fun getCsrfToken(): ApiResponse<Any>

    // Gas Leak Detection
    @GET("gas-leak/status/")
    suspend fun getGasLeakStatus(@Header("Authorization") token: String): ApiResponse<GasLeakData>

    @POST("gas-leak/scan/")
    suspend fun triggerGasScan(@Header("Authorization") token: String): ApiResponse<GasLeakData>

    // Gas Level Monitoring
    @GET("gas-level/data/")
    suspend fun getGasLevelData(@Header("Authorization") token: String): ApiResponse<GasLevelData>

    // Pipeline Health
    @GET("pipeline/health/")
    suspend fun getPipelineHealth(@Header("Authorization") token: String): ApiResponse<PipelineHealthData>

    // Regulator Control
    @GET("regulator/control/")
    suspend fun getRegulatorData(@Header("Authorization") token: String): ApiResponse<RegulatorData>

    @POST("regulator/control/")
    suspend fun controlRegulator(
        @Header("Authorization") token: String,
        @Body action: Map<String, String>
    ): ApiResponse<RegulatorData>

    // Emergency Contacts
    @GET("emergency/contacts/")
    suspend fun getEmergencyContacts(@Header("Authorization") token: String): ApiResponse<List<EmergencyContactData>>

    @POST("emergency/contacts/")
    suspend fun addEmergencyContact(
        @Header("Authorization") token: String,
        @Body contact: EmergencyContactRequest
    ): ApiResponse<EmergencyContactData>
    @DELETE("emergency/contacts/{contact_id}/delete/")
    suspend fun deleteEmergencyContact(
        @Path("contact_id") contactId: Int,
        @Header("Authorization") token: String
    ): ApiResponse<EmergencyContactData>

    @POST("emergency/sos/")
    suspend fun triggerEmergencySOS(@Header("Authorization") token: String): ApiResponse<Any>

    // Profile Management
    @GET("profile/")
    suspend fun getUserProfile(@Header("Authorization") token: String): ApiResponse<ProfileData>

    @PUT("profile/")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body profileData: Map<String, String>
    ): ApiResponse<ProfileData>

    @POST("profile/upload-image/")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Body imageData: Map<String, String>
    ): ApiResponse<ProfileImageResponse>
}


object NetworkService {
    const val BASE_URL = "http://10.0.2.2:8000/" // Django typically runs on port 8000
    // For physical device: "http://YOUR_COMPUTER_IP:8000/api/"


    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    suspend fun getCsrfToken(): String? {
        return try {
            val response = api.getCsrfToken()
            // Extract CSRF token from response (adjust based on your Django response format)
            (response.data as? Map<*, *>)?.get("csrfToken") as? String
        } catch (e: Exception) {
            Log.e("NetworkService", "Failed to get CSRF token", e)
            null
        }
    }


    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)


}
// Add these at the bottom of your NetworkService.kt file
fun Int?.toBooleanSafe(): Boolean = this == 1
fun Int?.isSuccess(): Boolean = this == 1 || this == 200
}

