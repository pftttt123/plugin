package com.setupnotebook.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LapExport(
    val lapTimeMs: Long,
    val conditions: String = "",
    val airTemp: Double? = null,
    val trackTemp: Double? = null,
    val loggedAt: Long = 0,
)

@Serializable
data class SetupExport(
    val name: String,
    val car: String,
    val simTitle: String,
    val track: String,
    val notes: String = "",
    val values: Map<String, Double> = emptyMap(),
    val laps: List<LapExport> = emptyList(),
)

@Serializable
data class ExportFile(
    val app: String = "setup-notebook",
    val version: Int = 1,
    val setups: List<SetupExport>,
)

object ExportImport {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(file: ExportFile): String = json.encodeToString(ExportFile.serializer(), file)

    fun decode(text: String): ExportFile = json.decodeFromString(ExportFile.serializer(), text)

    fun toExport(setup: Setup, car: Car?, track: Track?, laps: List<LapEntry>): SetupExport =
        SetupExport(
            name = setup.name,
            car = car?.name ?: "Unknown car",
            simTitle = car?.simTitle ?: "Other",
            track = track?.name ?: "Unknown track",
            notes = setup.notes,
            values = SetupFields.parseValues(setup.valuesJson),
            laps = laps.map { LapExport(it.lapTimeMs, it.conditions, it.airTemp, it.trackTemp, it.loggedAt) },
        )
}
