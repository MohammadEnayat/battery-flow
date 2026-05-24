package com.example.data.remote

import com.example.domain.model.BatteryTip
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BatteryTipResponse(
    val id: Int,
    val title: String,
    val content: String,
    val category: String
) {
    fun toDomain(): BatteryTip = BatteryTip(
        id = id,
        title = title,
        content = content,
        category = category
    )
}
