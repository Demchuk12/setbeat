package com.vadem.setbeat.data

import com.vadem.setbeat.data.db.HeartRateSampleDao
import com.vadem.setbeat.data.db.HeartRateSampleEntity
import com.vadem.setbeat.domain.HeartRateSample

class HeartRateRepository(private val dao: HeartRateSampleDao) {

    suspend fun save(sessionId: String, samples: List<HeartRateSample>) {
        if (samples.isEmpty()) return
        dao.insertAll(samples.map { HeartRateSampleEntity(sessionId = sessionId, bpm = it.bpm, timestamp = it.timestamp, accuracy = it.accuracy) })
    }

    suspend fun count(sessionId: String): Int = dao.count(sessionId)
}
