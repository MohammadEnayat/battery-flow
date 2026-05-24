package com.example.data.remote

import retrofit2.http.GET

interface BatteryTipsApi {
    @GET("battery-insights-tips")
    suspend fun getBatteryTips(): List<BatteryTipResponse>
}
