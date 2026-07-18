package com.kawaiical.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType(val label: String, val emoji: String) {
    BREAKFAST("Breakfast", "🍓"),
    LUNCH("Lunch", "🍱"),
    DINNER("Dinner", "🍜"),
    SNACK("Snacks", "🧁"),
}

/**
 * A food in the searchable local database. Bundled as a starter dataset;
 * the schema mirrors what a nutrition API would return so remote results
 * can be mapped straight into it later.
 */
@Entity(tableName = "foods")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val serving: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val isCustom: Boolean = false,
)

/** One logged food in the daily diary. Macros are snapshotted per entry. */
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodName: String,
    val mealType: MealType,
    val servings: Float,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val epochDay: Long,
    val loggedAt: Long,
)

/** Glasses of water drunk on a given day. */
@Entity(tableName = "water_days")
data class WaterDay(
    @PrimaryKey val epochDay: Long,
    val glasses: Int,
)

/** Aggregated calories for one day, used by the history chart. */
data class DayTotal(
    val epochDay: Long,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
)
