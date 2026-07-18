package com.kawaiical.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kawaiical.app.KawaiiCalApplication
import com.kawaiical.app.data.DiaryRepository
import com.kawaiical.app.data.db.DiaryEntry
import com.kawaiical.app.data.db.MealType
import com.kawaiical.app.data.prefs.UserPreferences
import com.kawaiical.app.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

data class HomeUiState(
    val loaded: Boolean = false,
    val entries: List<DiaryEntry> = emptyList(),
    val totalCalories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val prefs: UserPreferences = UserPreferences(),
    val waterGlasses: Int = 0,
    val streak: Int = 0,
) {
    val byMeal: Map<MealType, List<DiaryEntry>> = entries.groupBy { it.mealType }
    val goalReached: Boolean = loaded && totalCalories >= prefs.calorieGoal && totalCalories > 0
    val remaining: Int = (prefs.calorieGoal - totalCalories).coerceAtLeast(0)
}

class HomeViewModel(
    private val repo: DiaryRepository,
    private val prefsRepo: UserPreferencesRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    val state: StateFlow<HomeUiState> = combine(
        repo.entriesForDay(today),
        prefsRepo.preferences,
        repo.waterForDay(today),
        repo.streak(today),
    ) { entries, prefs, water, streak ->
        HomeUiState(
            loaded = true,
            entries = entries,
            totalCalories = entries.sumOf { it.calories },
            protein = entries.map { it.protein }.sum(),
            carbs = entries.map { it.carbs }.sum(),
            fat = entries.map { it.fat }.sum(),
            prefs = prefs,
            waterGlasses = water,
            streak = streak,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun addWater(delta: Int) {
        val current = state.value.waterGlasses
        viewModelScope.launch { repo.setWater(today, current + delta) }
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch { repo.deleteEntry(entry) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as KawaiiCalApplication
                HomeViewModel(app.container.diaryRepository, app.container.preferencesRepository)
            }
        }
    }
}
