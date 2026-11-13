package com.vadem.setbeat.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity)

    @Query("UPDATE session SET ended_at = :endedAt WHERE id = :id")
    suspend fun end(id: String, endedAt: Long)

    @Query("SELECT * FROM session WHERE ended_at IS NULL ORDER BY started_at DESC LIMIT 1")
    suspend fun findOpen(): SessionEntity?

    @Query("SELECT * FROM session WHERE id = :id")
    suspend fun get(id: String): SessionEntity?
}

@Dao
interface HeartRateSampleDao {
    @Insert
    suspend fun insertAll(samples: List<HeartRateSampleEntity>)

    @Query("SELECT COUNT(*) FROM hr_sample WHERE session_id = :sessionId")
    suspend fun count(sessionId: String): Int

    @Query("SELECT * FROM hr_sample WHERE session_id = :sessionId ORDER BY timestamp")
    suspend fun forSession(sessionId: String): List<HeartRateSampleEntity>
}

@Dao
interface WorkoutEventDao {
    @Insert
    suspend fun insert(event: WorkoutEventEntity)

    @Query("SELECT COALESCE(MAX(seq) + 1, 0) FROM workout_event WHERE session_id = :sessionId")
    suspend fun nextSeq(sessionId: String): Int

    @Query("SELECT * FROM workout_event WHERE session_id = :sessionId ORDER BY seq")
    suspend fun forSession(sessionId: String): List<WorkoutEventEntity>
}
