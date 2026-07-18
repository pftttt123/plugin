package com.kawaiical.app.data

import com.kawaiical.app.data.db.AppDatabase
import com.kawaiical.app.data.db.DayTotal
import com.kawaiical.app.data.db.DiaryEntry
import com.kawaiical.app.data.db.FoodItem
import com.kawaiical.app.data.db.MealType
import com.kawaiical.app.data.db.SeedFoods
import com.kawaiical.app.data.db.WaterDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import kotlin.math.roundToInt

class DiaryRepository(private val db: AppDatabase) {

    suspend fun seedIfEmpty() {
        if (db.foodDao().count() == 0) {
            db.foodDao().insertAll(SeedFoods.items)
        }
    }

    fun searchFoods(query: String): Flow<List<FoodItem>> = db.foodDao().search(query)

    suspend fun addCustomFood(food: FoodItem): Long =
        db.foodDao().insert(food.copy(isCustom = true))

    fun entriesForDay(date: LocalDate): Flow<List<DiaryEntry>> =
        db.diaryDao().entriesForDay(date.toEpochDay())

    suspend fun logFood(food: FoodItem, mealType: MealType, servings: Float, date: LocalDate) {
        db.diaryDao().insert(
            DiaryEntry(
                foodName = food.name,
                mealType = mealType,
                servings = servings,
                calories = (food.calories * servings).roundToInt(),
                protein = food.protein * servings,
                carbs = food.carbs * servings,
                fat = food.fat * servings,
                epochDay = date.toEpochDay(),
                loggedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun deleteEntry(entry: DiaryEntry) = db.diaryDao().delete(entry)

    fun totalsForLastDays(days: Int, today: LocalDate): Flow<List<DayTotal>> {
        val to = today.toEpochDay()
        val from = to - (days - 1)
        return db.diaryDao().totalsBetween(from, to).map { totals ->
            // Fill gaps so the chart always has one point per day
            val byDay = totals.associateBy { it.epochDay }
            (from..to).map { day ->
                byDay[day] ?: DayTotal(day, 0, 0f, 0f, 0f)
            }
        }
    }

    /**
     * Consecutive days with at least one logged food, counting back from
     * today (or yesterday, so the streak isn't broken before breakfast).
     */
    fun streak(today: LocalDate): Flow<Int> =
        db.diaryDao().loggedDays().map { days ->
            val logged = days.toHashSet()
            val todayEpoch = today.toEpochDay()
            var start = when {
                todayEpoch in logged -> todayEpoch
                (todayEpoch - 1) in logged -> todayEpoch - 1
                else -> return@map 0
            }
            var count = 0
            while (start in logged) {
                count++
                start--
            }
            count
        }

    fun waterForDay(date: LocalDate): Flow<Int> =
        db.waterDao().forDay(date.toEpochDay()).map { it?.glasses ?: 0 }

    suspend fun setWater(date: LocalDate, glasses: Int) {
        db.waterDao().upsert(WaterDay(date.toEpochDay(), glasses.coerceIn(0, 30)))
    }
}
