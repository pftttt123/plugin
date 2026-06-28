package com.carspotter.data.remote

import retrofit2.http.GET

/**
 * Backend catalog API. The app loads the whole catalog once and does search /
 * filtering / progress locally, so browsing stays fast and works offline after
 * the first load.
 */
interface ApiService {
    @GET("catalog")
    suspend fun getCatalog(): List<ManufacturerDto>
}
