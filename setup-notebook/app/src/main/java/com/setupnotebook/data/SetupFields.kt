package com.setupnotebook.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlin.math.roundToLong

/** Definition of a single numeric setup field. */
data class FieldDef(
    val key: String,
    val label: String,
    val unit: String,
    val min: Double,
    val max: Double,
    val step: Double,
    val decimals: Int,
    val default: Double = (min + max) / 2,
)

/** A row in the setup editor: either one field or a front-left/front-right/rear-left/rear-right group. */
sealed interface SetupRow {
    data class Single(val field: FieldDef) : SetupRow
    data class Corners(val label: String, val unit: String, val fields: List<FieldDef>) : SetupRow
}

data class SectionDef(
    val key: String,
    val title: String,
    val rows: List<SetupRow>,
) {
    val fields: List<FieldDef>
        get() = rows.flatMap {
            when (it) {
                is SetupRow.Single -> listOf(it.field)
                is SetupRow.Corners -> it.fields
            }
        }
}

/** Static registry of every editable setup field, grouped into sections. */
object SetupFields {

    val simTitles = listOf("iRacing", "ACC", "AC", "AC Evo", "rFactor 2", "LMU", "F1", "GT7", "Other")

    private fun corners(base: String, label: String, unit: String, min: Double, max: Double, step: Double, decimals: Int, default: Double): SetupRow.Corners =
        SetupRow.Corners(
            label = label,
            unit = unit,
            fields = listOf("fl", "fr", "rl", "rr").map { c ->
                FieldDef("${base}_$c", "${label} ${c.uppercase()}", unit, min, max, step, decimals, default)
            },
        )

    private fun single(key: String, label: String, unit: String, min: Double, max: Double, step: Double, decimals: Int, default: Double = (min + max) / 2): SetupRow.Single =
        SetupRow.Single(FieldDef(key, label, unit, min, max, step, decimals, default))

    val sections: List<SectionDef> = listOf(
        SectionDef(
            key = "tyres",
            title = "Tyres",
            rows = listOf(
                corners("tyre_press", "Pressure", "psi", 12.0, 40.0, 0.1, 1, 26.0),
                corners("camber", "Camber", "°", -8.0, 2.0, 0.1, 1, -3.0),
                corners("toe", "Toe", "°", -1.0, 1.0, 0.01, 2, 0.0),
            ),
        ),
        SectionDef(
            key = "suspension",
            title = "Suspension",
            rows = listOf(
                corners("spring", "Spring", "N/mm", 10.0, 400.0, 1.0, 0, 120.0),
                corners("bump", "Bump", "clk", 0.0, 40.0, 1.0, 0, 10.0),
                corners("rebound", "Rebound", "clk", 0.0, 40.0, 1.0, 0, 10.0),
                single("arb_front", "ARB front", "", 0.0, 10.0, 1.0, 0, 4.0),
                single("arb_rear", "ARB rear", "", 0.0, 10.0, 1.0, 0, 3.0),
                single("ride_height_front", "Ride height F", "mm", 30.0, 150.0, 1.0, 0, 55.0),
                single("ride_height_rear", "Ride height R", "mm", 30.0, 150.0, 1.0, 0, 65.0),
            ),
        ),
        SectionDef(
            key = "aero",
            title = "Aero",
            rows = listOf(
                single("wing_front", "Front wing", "", 0.0, 40.0, 1.0, 0, 4.0),
                single("wing_rear", "Rear wing", "", 0.0, 40.0, 1.0, 0, 6.0),
                single("splitter", "Splitter", "", 0.0, 10.0, 1.0, 0, 2.0),
            ),
        ),
        SectionDef(
            key = "drivetrain",
            title = "Drivetrain",
            rows = listOf(
                single("diff_preload", "Diff preload", "Nm", 0.0, 300.0, 5.0, 0, 60.0),
                single("diff_power", "Diff power", "%", 0.0, 100.0, 5.0, 0, 50.0),
                single("diff_coast", "Diff coast", "%", 0.0, 100.0, 5.0, 0, 30.0),
                single("final_drive", "Final drive", "", 2.0, 7.0, 0.001, 3, 3.5),
                single("gear_1", "Gear 1", "", 1.0, 5.0, 0.001, 3, 2.8),
                single("gear_2", "Gear 2", "", 0.8, 4.0, 0.001, 3, 2.2),
                single("gear_3", "Gear 3", "", 0.7, 3.5, 0.001, 3, 1.8),
                single("gear_4", "Gear 4", "", 0.6, 3.0, 0.001, 3, 1.5),
                single("gear_5", "Gear 5", "", 0.5, 2.5, 0.001, 3, 1.25),
                single("gear_6", "Gear 6", "", 0.4, 2.0, 0.001, 3, 1.05),
                single("gear_7", "Gear 7", "", 0.3, 1.8, 0.001, 3, 0.9),
                single("gear_8", "Gear 8", "", 0.3, 1.6, 0.001, 3, 0.8),
            ),
        ),
        SectionDef(
            key = "brakes",
            title = "Brakes",
            rows = listOf(
                single("brake_bias", "Bias front", "%", 40.0, 75.0, 0.1, 1, 57.0),
                single("brake_pressure", "Pressure", "%", 50.0, 120.0, 1.0, 0, 100.0),
                single("duct_front", "Duct front", "", 0.0, 6.0, 1.0, 0, 2.0),
                single("duct_rear", "Duct rear", "", 0.0, 6.0, 1.0, 0, 2.0),
            ),
        ),
        SectionDef(
            key = "fuel",
            title = "Fuel & Strategy",
            rows = listOf(
                single("fuel_load", "Fuel load", "L", 0.0, 150.0, 1.0, 0, 40.0),
                single("fuel_per_lap", "Fuel per lap", "L", 0.0, 10.0, 0.01, 2, 2.8),
                single("stint_laps", "Stint length", "laps", 0.0, 120.0, 1.0, 0, 20.0),
                single("tyre_sets", "Tyre sets", "", 0.0, 20.0, 1.0, 0, 1.0),
            ),
        ),
    )

    val allFields: List<FieldDef> = sections.flatMap { it.fields }
    val byKey: Map<String, FieldDef> = allFields.associateBy { it.key }

    private val json = Json { ignoreUnknownKeys = true }

    fun parseValues(valuesJson: String): Map<String, Double> = runCatching {
        json.parseToJsonElement(valuesJson).jsonObject
            .mapNotNull { (k, v) -> (v as? JsonPrimitive)?.doubleOrNull?.let { k to it } }
            .toMap()
    }.getOrDefault(emptyMap())

    fun encodeValues(values: Map<String, Double>): String =
        buildJsonObject {
            values.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
        }.toString()

    fun format(field: FieldDef, value: Double): String =
        if (field.decimals == 0) value.roundToLong().toString()
        else String.format("%.${field.decimals}f", value)
}

/** Format milliseconds as m:ss.mmm */
fun formatLapTime(ms: Long): String {
    val minutes = ms / 60_000
    val seconds = (ms % 60_000) / 1000
    val millis = ms % 1000
    return "%d:%02d.%03d".format(minutes, seconds, millis)
}

/** Parse "1:23.456", "83.456" or "1.23.456" style lap times to milliseconds. */
fun parseLapTime(text: String): Long? {
    val t = text.trim().replace(',', '.')
    val parts = t.split(":")
    return runCatching {
        when (parts.size) {
            1 -> (parts[0].toDouble() * 1000).roundToLong()
            2 -> parts[0].toLong() * 60_000 + (parts[1].toDouble() * 1000).roundToLong()
            else -> null
        }
    }.getOrNull()?.takeIf { it > 0 }
}
