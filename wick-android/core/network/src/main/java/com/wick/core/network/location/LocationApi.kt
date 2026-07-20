package com.wick.core.network.location

import retrofit2.http.GET
import retrofit2.http.Path

interface LocationApi {

    @GET("api/v1/locations/provinces")
    suspend fun getProvinces(): List<String>

    @GET("api/v1/locations/provinces/{province}/districts")
    suspend fun getDistricts(@Path("province") province: String): List<String>
}
