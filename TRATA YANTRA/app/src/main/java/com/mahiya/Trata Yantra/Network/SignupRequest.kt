package com.mahiya.safegas.network

import com.google.gson.annotations.SerializedName

data class SignupRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("device_unique_code")
    val device_unique_code: String
)
