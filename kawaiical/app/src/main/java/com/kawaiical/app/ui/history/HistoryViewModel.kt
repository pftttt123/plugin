package com.kawaiical.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kawaiical.app.KawaiiCalApplication
import com.kawaiical.app.data.DiaryRepository
import com.kawaiical.app.data.db.DayTotal
import com.kawaiical.app.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HistoryUiState(
    val loaded: Boolean = false,
    val days: List<DayTotal> = emptyList(),
    val calorieGoal: Int = 2200,
    val streak: Int = 0,
) {
    val average: Int =
        days.filter { it.calories > 0 }.let { logged ->
            if (logged.isEmpty()) 0 else logged.sumOf { it.calories } / logged.size
        }
    val best: Int = days.count { it.calories in 1..calorieGoal }
}

class HistoryViewModel(
    repo: DiaryRepository,
    prefsRepo: UserPreferencesRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    val state: StateFlow<HistoryUiState> = combine(
        repo.totalsForLastDays(7, today),
        prefsRepo.preferences,
        repo.streak(today),
    ) { days, prefs, streak ->
        HistoryUiState(
            loaded = true,
            days = days,
            calorieGoal = prefs.calorieGoal,
            streak = streak,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as KawaiiCalApplication
                HistoryViewModel(app.container.diaryRepository, app.container.preferencesRepository)
            }
        }
    }
}
