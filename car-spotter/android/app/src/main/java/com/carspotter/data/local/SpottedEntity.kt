package com.carspotter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per spotted car. Presence == spotted; row removed when un-spotted. */
@Entity(tableName = "spotted")
data class SpottedEntity(
    @PrimaryKey val carId: Int,
    val spottedAt: Long,
)
