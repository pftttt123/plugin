package com.carspotter.data.remote

import com.squareup.moshi.Json

/** A single car as returned by the backend (snake_case JSON). */
data class CarDto(
    val id: Int,
    @Json(name = "manufacturer_id") val manufacturerId: Int,
    @Json(name = "manufacturer_name") val manufacturerName: String,
    val model: String,
    @Json(name = "year_start") val yearStart: Int?,
    @Json(name = "year_end") val yearEnd: Int?,
    val generation: String?,
    @Json(name = "body_type") val bodyType: String?,
    @Json(name = "image_url") val imageUrl: String?,
)

/** A manufacturer with its nested cars, from GET /catalog. */
data class ManufacturerDto(
    val id: Int,
    val name: String,
    val country: String?,
    @Json(name = "logo_url") val logoUrl: String?,
    @Json(name = "car_count") val carCount: Int,
    val cars: List<CarDto> = emptyList(),
)
