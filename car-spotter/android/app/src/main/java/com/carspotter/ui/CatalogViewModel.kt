package com.carspotter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.carspotter.CarSpotterApp
import com.carspotter.data.remote.CarDto
import com.carspotter.data.remote.ManufacturerDto
import com.carspotter.data.repository.CarRepository
import com.carspotter.ui.model.CarUi
import com.carspotter.ui.model.CatalogUiState
import com.carspotter.ui.model.ManufacturerGroup
import com.carspotter.ui.model.SpottedFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogViewModel(private val repository: CarRepository) : ViewModel() {

    private data class Load(val loading: Boolean = true, val error: String? = null)

    private val rawCatalog = MutableStateFlow<List<ManufacturerDto>>(emptyList())
    private val load = MutableStateFlow(Load())

    val query = MutableStateFlow("")
    val filter = MutableStateFlow(SpottedFilter.ALL)

    /** All cars (unfiltered) with spotted state — used by the detail screen. */
    val allCars: StateFlow<List<CarUi>> =
        combine(rawCatalog, repository.spottedIds()) { makes, spotted ->
            makes.flatMap { m -> m.cars.map { it.toUi(spotted.contains(it.id)) } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<CatalogUiState> =
        combine(rawCatalog, repository.spottedIds(), query, filter, load) { makes, spotted, q, f, l ->
            buildState(makes, spotted, q.trim(), f, l)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            load.value = Load(loading = true, error = null)
            try {
                rawCatalog.value = repository.fetchCatalog()
                load.value = Load(loading = false, error = null)
            } catch (e: Exception) {
                load.value = Load(
                    loading = false,
                    error = "Couldn't reach the backend. Is it running and is BASE_URL correct?",
                )
            }
        }
    }

    fun setQuery(value: String) = query.update { value }

    fun setFilter(value: SpottedFilter) = filter.update { value }

    fun toggleSpotted(car: CarUi) {
        viewModelScope.launch { repository.setSpotted(car.id, !car.spotted) }
    }

    private fun buildState(
        makes: List<ManufacturerDto>,
        spotted: Set<Int>,
        q: String,
        f: SpottedFilter,
        l: Load,
    ): CatalogUiState {
        val total = makes.sumOf { it.cars.size }
        val spottedCount = makes.sumOf { m -> m.cars.count { spotted.contains(it.id) } }

        val groups = makes.mapNotNull { m ->
            val cars = m.cars
                .filter { car -> matchesQuery(m.name, car.model, q) }
                .map { it.toUi(spotted.contains(it.id)) }
                .filter { car ->
                    when (f) {
                        SpottedFilter.ALL -> true
                        SpottedFilter.SPOTTED -> car.spotted
                        SpottedFilter.NOT_SPOTTED -> !car.spotted
                    }
                }
            if (cars.isEmpty()) null
            else ManufacturerGroup(id = m.id, name = m.name, country = m.country, cars = cars)
        }

        return CatalogUiState(
            isLoading = l.loading,
            error = l.error,
            query = q,
            filter = f,
            groups = groups,
            totalCars = total,
            spottedCount = spottedCount,
        )
    }

    private fun matchesQuery(make: String, model: String, q: String): Boolean {
        if (q.isBlank()) return true
        return make.contains(q, ignoreCase = true) || model.contains(q, ignoreCase = true)
    }

    private fun CarDto.toUi(spotted: Boolean) = CarUi(
        id = id,
        manufacturerId = manufacturerId,
        manufacturerName = manufacturerName,
        model = model,
        yearStart = yearStart,
        yearEnd = yearEnd,
        generation = generation,
        bodyType = bodyType,
        imageUrl = imageUrl,
        spotted = spotted,
    )

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CarSpotterApp
                return CatalogViewModel(app.container.repository) as T
            }
        }
    }
}
