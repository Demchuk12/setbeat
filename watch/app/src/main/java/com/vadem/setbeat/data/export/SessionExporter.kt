package com.vadem.setbeat.data.export

import com.vadem.setbeat.data.db.HeartRateSampleDao
import com.vadem.setbeat.data.db.SessionDao
import com.vadem.setbeat.data.db.WorkoutEventDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SessionExporter(
    private val sessions: SessionDao,
    private val events: WorkoutEventDao,
    private val samples: HeartRateSampleDao,
    private val directory: File,
) {
    suspend fun export(sessionId: String): File? {
        val session = sessions.get(sessionId) ?: return null
        val export = SessionExport.of(session, events.forSession(sessionId), samples.forSession(sessionId))
        return withContext(Dispatchers.IO) {
            directory.mkdirs()
            File(directory, "session-$sessionId.json").apply { writeText(export.encode()) }
        }
    }
}
