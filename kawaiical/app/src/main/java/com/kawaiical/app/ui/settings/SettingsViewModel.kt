package com.kawaiical.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kawaiical.app.KawaiiCalApplication
import com.kawaiical.app.data.prefs.ThemeMode
import com.kawaiical.app.data.prefs.Units
import com.kawaiical.app.data.prefs.UserPreferences
import com.kawaiical.app.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefsRepo: UserPreferencesRepository,
) : ViewModel() {

    val prefs: StateFlow<UserPreferences> = prefsRepo.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun setCalorieGoal(goal: Int) = viewModelScope.launch { prefsRepo.setCalorieGoal(goal) }
    fun setWaterGoal(goal: Int) = viewModelScope.launch { prefsRepo.setWaterGoal(goal) }
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    fun setUnits(units: Units) = viewModelScope.launch { prefsRepo.setUnits(units) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as KawaiiCalApplication
                SettingsViewModel(app.container.preferencesRepository)
            }
        }
    }
}
