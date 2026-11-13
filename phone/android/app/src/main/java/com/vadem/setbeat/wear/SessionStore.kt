package com.vadem.setbeat.wear

import org.json.JSONObject
import java.io.File

data class StoredSession(val id: String, val startedAt: Long, val endedAt: Long?, val receivedAt: Long)

class SessionStore(private val directory: File) {

    fun save(id: String, bytes: ByteArray): Boolean {
        require(SAFE_ID.matches(id)) { "invalid session id $id" }
        val parsed = JSONObject(String(bytes))
        require(parsed.getString("id") == id) { "session id mismatch" }
        directory.mkdirs()
        val target = file(id)
        if (target.exists()) return false
        val temp = File(directory, "$id$SUFFIX.tmp")
        temp.writeBytes(bytes)
        check(temp.renameTo(target)) { "could not store session $id" }
        return true
    }

    fun list(): List<StoredSession> =
        directory.listFiles { f -> f.name.endsWith(SUFFIX) }.orEmpty()
            .mapNotNull { f -> runCatching { summary(f) }.getOrNull() }
            .sortedByDescending { it.startedAt }

    fun read(id: String): String? = file(id).takeIf { SAFE_ID.matches(id) && it.exists() }?.readText()

    private fun summary(f: File): StoredSession? {
        val id = f.name.removeSuffix(SUFFIX)
        if (!SAFE_ID.matches(id)) return null
        val json = JSONObject(f.readText())
        if (json.getString("id") != id) return null
        return StoredSession(
            id = id,
            startedAt = json.getLong("startedAt"),
            endedAt = if (json.isNull("endedAt")) null else json.getLong("endedAt"),
            receivedAt = f.lastModified(),
        )
    }

    private fun file(id: String) = File(directory, "$id$SUFFIX")

    companion object {
        private const val SUFFIX = ".json"
        private val SAFE_ID = Regex("[A-Za-z0-9-]{1,64}")
    }
}
