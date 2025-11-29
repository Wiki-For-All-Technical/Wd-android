package com.example.wikiapp.data.api

import com.example.wikiapp.data.model.TopReadResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface WikimediaRestService {
    // Example: /api/rest_v1/metrics/pageviews/top/en.wikipedia/all-access/2025/11/11
    @GET("api/rest_v1/metrics/pageviews/top/{project}/all-access/{year}/{month}/{day}")
    suspend fun getTopRead(
        @Path("project") project: String = "en.wikipedia",
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): Response<TopReadResponse>
}






