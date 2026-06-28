package com.carspotter.data.repository

import com.carspotter.data.local.SpottedDao
import com.carspotter.data.local.SpottedEntity
import com.carspotter.data.remote.ApiService
import com.carspotter.data.remote.ManufacturerDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Bridges the remote catalog API and the local "spotted" store. */
class CarRepository(
    private val api: ApiService,
    private val spottedDao: SpottedDao,
) {
    suspend fun fetchCatalog(): List<ManufacturerDto> = api.getCatalog()

    /** Set of car ids the user has spotted, observed reactively. */
    fun spottedIds(): Flow<Set<Int>> = spottedDao.spottedIds().map { it.toSet() }

    suspend fun setSpotted(carId: Int, spotted: Boolean) {
        if (spotted) {
            spottedDao.insert(SpottedEntity(carId = carId, spottedAt = System.currentTimeMillis()))
        } else {
            spottedDao.delete(carId)
        }
    }
}
