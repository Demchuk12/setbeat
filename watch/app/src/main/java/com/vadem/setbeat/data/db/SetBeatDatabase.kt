package com.vadem.setbeat.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec

@Database(
    entities = [SessionEntity::class, HeartRateSampleEntity::class, WorkoutEventEntity::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = SetBeatDatabase.DropSpikeEvents::class),
    ],
)
abstract class SetBeatDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    abstract fun heartRateSampleDao(): HeartRateSampleDao

    abstract fun workoutEventDao(): WorkoutEventDao

    @DeleteTable(tableName = "event")
    class DropSpikeEvents : AutoMigrationSpec

    companion object {
        fun create(context: Context): SetBeatDatabase =
            Room.databaseBuilder(context.applicationContext, SetBeatDatabase::class.java, "setbeat.db").build()
    }
}
