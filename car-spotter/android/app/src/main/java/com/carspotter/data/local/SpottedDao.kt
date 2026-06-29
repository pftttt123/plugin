package com.carspotter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpottedDao {
    /** Emits the set of spotted car ids, updating whenever it changes. */
    @Query("SELECT carId FROM spotted")
    fun spottedIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SpottedEntity)

    @Query("DELETE FROM spotted WHERE carId = :carId")
    suspend fun delete(carId: Int)
}
