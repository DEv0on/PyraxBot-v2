package com.dev0on.common.modules.automation.videoapi

import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory
import reactor.core.publisher.Mono
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface VideoService {
    @GET("tiktok/normal/{user}/{id}")
    fun getVideo(@Path("user") user: String, @Path("id") id: String): Mono<VideoResponse>

    @GET("tiktok/short/{id}")
    fun getShortVideo(@Path("id") id: String): Mono<VideoResponse>

    @GET("reel/{id}")
    fun getReel(@Path("id") id: String): Mono<VideoResponse>

    companion object {
        private val url = System.getenv("IG_TT_API_BASE_URL")

        private val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val client = retrofit.create(VideoService::class.java)
    }

    data class VideoResponse(val url: String, val video_url: String)
}