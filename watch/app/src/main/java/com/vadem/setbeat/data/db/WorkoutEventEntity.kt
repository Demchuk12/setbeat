package com.vadem.setbeat.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_event",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["session_id", "seq"], unique = true)],
)
data class WorkoutEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "session_id") val sessionId: String,
    val seq: Int,
    val type: String,
    val payload: String,
    val timestamp: Long,
    @ColumnInfo(name = "device_id") val deviceId: String,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int,
)
