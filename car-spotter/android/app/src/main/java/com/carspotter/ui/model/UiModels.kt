package com.carspotter.ui.model

/** A car prepared for display, including whether it's been spotted. */
data class CarUi(
    val id: Int,
    val manufacturerId: Int,
    val manufacturerName: String,
    val model: String,
    val yearStart: Int?,
    val yearEnd: Int?,
    val generation: String?,
    val bodyType: String?,
    val imageUrl: String?,
    val spotted: Boolean,
) {
    /** e.g. "2017–present", "1989–2005", or "2019". */
    val yearsLabel: String
        get() = when {
            yearStart == null -> ""
            yearEnd == null -> "$yearStart–present"
            yearStart == yearEnd -> "$yearStart"
            else -> "$yearStart–$yearEnd"
        }

    /** e.g. "Mk8 · Hatchback". */
    val detailLabel: String
        get() = listOfNotNull(generation?.takeIf { it.isNotBlank() }, bodyType?.takeIf { it.isNotBlank() })
            .joinToString(" · ")
}

/** A manufacturer header plus its (possibly filtered) cars. */
data class ManufacturerGroup(
    val id: Int,
    val name: String,
    val country: String?,
    val cars: List<CarUi>,
)

enum class SpottedFilter { ALL, SPOTTED, NOT_SPOTTED }

data class CatalogUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val query: String = "",
    val filter: SpottedFilter = SpottedFilter.ALL,
    val groups: List<ManufacturerGroup> = emptyList(),
    val totalCars: Int = 0,
    val spottedCount: Int = 0,
) {
    val remainingCount: Int get() = totalCars - spottedCount
    val progressFraction: Float
        get() = if (totalCars == 0) 0f else spottedCount.toFloat() / totalCars
}
