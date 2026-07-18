package com.kawaiical.app.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kawaiical.app.KawaiiCalApplication
import com.kawaiical.app.data.DiaryRepository
import com.kawaiical.app.data.db.FoodItem
import com.kawaiical.app.data.db.MealType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LogFoodViewModel(
    private val repo: DiaryRepository,
    initialMeal: MealType,
) : ViewModel() {

    val query = MutableStateFlow("")
    val selectedMeal = MutableStateFlow(initialMeal)

    val results: StateFlow<List<FoodItem>> = query
        .debounce(120)
        .flatMapLatest { repo.searchFoods(it.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun log(food: FoodItem, servings: Float, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.logFood(food, selectedMeal.value, servings, LocalDate.now())
            onDone()
        }
    }

    /** Save a custom food into the database, then log it right away. */
    fun quickAdd(
        name: String,
        serving: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val food = FoodItem(
                name = name,
                serving = serving.ifBlank { "1 serving" },
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
            )
            repo.addCustomFood(food)
            repo.logFood(food, selectedMeal.value, 1f, LocalDate.now())
            onDone()
        }
    }

    companion object {
        fun factory(initialMeal: MealType) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as KawaiiCalApplication
                LogFoodViewModel(app.container.diaryRepository, initialMeal)
            }
        }
    }
}
