package com.example.aliro

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("user")
    fun getData(): Call<List<DataModels>>

    @POST("user")
    fun postData(@Body data: DataModels): Call<DataModels>
}
