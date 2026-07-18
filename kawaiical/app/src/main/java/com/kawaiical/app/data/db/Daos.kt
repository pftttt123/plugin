package com.kawaiical.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query(
        "SELECT * FROM foods WHERE name LIKE '%' || :query || '%' " +
            "ORDER BY isCustom DESC, name ASC LIMIT 60"
    )
    fun search(query: String): Flow<List<FoodItem>>

    @Insert
    suspend fun insert(food: FoodItem): Long

    @Insert
    suspend fun insertAll(foods: List<FoodItem>)

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun count(): Int
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE epochDay = :epochDay ORDER BY loggedAt ASC")
    fun entriesForDay(epochDay: Long): Flow<List<DiaryEntry>>

    @Insert
    suspend fun insert(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)

    @Query(
        "SELECT epochDay, SUM(calories) AS calories, SUM(protein) AS protein, " +
            "SUM(carbs) AS carbs, SUM(fat) AS fat FROM diary_entries " +
            "WHERE epochDay BETWEEN :from AND :to GROUP BY epochDay"
    )
    fun totalsBetween(from: Long, to: Long): Flow<List<DayTotal>>

    @Query("SELECT DISTINCT epochDay FROM diary_entries ORDER BY epochDay DESC")
    fun loggedDays(): Flow<List<Long>>
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_days WHERE epochDay = :epochDay")
    fun forDay(epochDay: Long): Flow<WaterDay?>

    @Upsert
    suspend fun upsert(day: WaterDay)
}
