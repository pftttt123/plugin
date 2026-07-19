package com.setupnotebook.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val simTitle: String,
)

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(
    tableName = "setups",
    foreignKeys = [
        ForeignKey(
            entity = Car::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("carId"), Index("trackId")],
)
data class Setup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val carId: Long,
    val trackId: Long,
    val notes: String = "",
    /** JSON object mapping field key -> numeric value, see [SetupFields]. */
    val valuesJson: String = "{}",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "laps",
    foreignKeys = [
        ForeignKey(
            entity = Setup::class,
            parentColumns = ["id"],
            childColumns = ["setupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("setupId")],
)
data class LapEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val setupId: Long,
    val lapTimeMs: Long,
    val conditions: String = "",
    val airTemp: Double? = null,
    val trackTemp: Double? = null,
    val loggedAt: Long = System.currentTimeMillis(),
)
