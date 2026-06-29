package com.carspotter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SpottedEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spottedDao(): SpottedDao
}
