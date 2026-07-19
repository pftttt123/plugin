package com.setupnotebook.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY simTitle, name")
    fun all(): Flow<List<Car>>

    @Insert
    suspend fun insert(car: Car): Long

    @Update
    suspend fun update(car: Car)

    @Delete
    suspend fun delete(car: Car)

    @Query("SELECT * FROM cars WHERE name = :name AND simTitle = :sim LIMIT 1")
    suspend fun findByNameAndSim(name: String, sim: String): Car?
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY name")
    fun all(): Flow<List<Track>>

    @Insert
    suspend fun insert(track: Track): Long

    @Update
    suspend fun update(track: Track)

    @Delete
    suspend fun delete(track: Track)

    @Query("SELECT * FROM tracks WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): Track?
}

@Dao
interface SetupDao {
    @Query("SELECT * FROM setups ORDER BY updatedAt DESC")
    fun all(): Flow<List<Setup>>

    @Query("SELECT * FROM setups WHERE id = :id")
    fun byId(id: Long): Flow<Setup?>

    @Query("SELECT * FROM setups WHERE id = :id")
    suspend fun byIdOnce(id: Long): Setup?

    @Insert
    suspend fun insert(setup: Setup): Long

    @Update
    suspend fun update(setup: Setup)

    @Delete
    suspend fun delete(setup: Setup)
}

@Dao
interface LapDao {
    @Query("SELECT * FROM laps ORDER BY loggedAt DESC")
    fun all(): Flow<List<LapEntry>>

    @Query("SELECT * FROM laps WHERE setupId = :setupId ORDER BY loggedAt DESC")
    fun forSetup(setupId: Long): Flow<List<LapEntry>>

    @Insert
    suspend fun insert(lap: LapEntry): Long

    @Delete
    suspend fun delete(lap: LapEntry)
}
