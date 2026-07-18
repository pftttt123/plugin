package com.kawaiical.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class Units { METRIC, IMPERIAL }

data class UserPreferences(
    val calorieGoal: Int = 2200,
    val waterGoal: Int = 8,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val units: Units = Units.METRIC,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val CALORIE_GOAL = intPreferencesKey("calorie_goal")
        val WATER_GOAL = intPreferencesKey("water_goal")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val UNITS = stringPreferencesKey("units")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            calorieGoal = prefs[Keys.CALORIE_GOAL] ?: 2200,
            waterGoal = prefs[Keys.WATER_GOAL] ?: 8,
            themeMode = prefs[Keys.THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            units = prefs[Keys.UNITS]
                ?.let { runCatching { Units.valueOf(it) }.getOrNull() }
                ?: Units.METRIC,
        )
    }

    suspend fun setCalorieGoal(goal: Int) {
        context.dataStore.edit { it[Keys.CALORIE_GOAL] = goal.coerceIn(800, 6000) }
    }

    suspend fun setWaterGoal(goal: Int) {
        context.dataStore.edit { it[Keys.WATER_GOAL] = goal.coerceIn(1, 20) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setUnits(units: Units) {
        context.dataStore.edit { it[Keys.UNITS] = units.name }
    }
}
