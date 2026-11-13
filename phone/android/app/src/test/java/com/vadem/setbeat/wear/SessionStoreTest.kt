package com.vadem.setbeat.wear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SessionStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun store() = SessionStore(folder.root)

    private fun session(id: String, startedAt: Long) =
        """{"id":"$id","startedAt":$startedAt,"endedAt":null,"events":[],"samples":[]}""".toByteArray()

    @Test
    fun `stores a session once`() {
        val store = store()

        assertTrue(store.save("a-1", session("a-1", 10)))
        assertFalse(store.save("a-1", session("a-1", 10)))
        assertEquals(listOf("a-1"), store.list().map { it.id })
    }

    @Test
    fun `lists newest sessions first`() {
        val store = store()
        store.save("old", session("old", 10))
        store.save("new", session("new", 20))

        assertEquals(listOf("new", "old"), store.list().map { it.id })
        assertNull(store.list().first().endedAt)
    }

    @Test
    fun `rejects mismatched or unsafe ids`() {
        val store = store()

        assertThrows(IllegalArgumentException::class.java) { store.save("a", session("b", 1)) }
        assertThrows(IllegalArgumentException::class.java) { store.save("../x", session("../x", 1)) }
        assertNull(store.read("../x"))
    }

    @Test
    fun `skips files whose name does not match the session inside`() {
        val store = store()
        store.save("a", session("a", 1))
        folder.root.resolve(".json").writeBytes(session("a", 1))
        folder.root.resolve("b.json").writeBytes(session("a", 1))
        folder.root.resolve("broken.json").writeText("{")

        assertEquals(listOf("a"), store.list().map { it.id })
    }

    @Test
    fun `reads stored json`() {
        val store = store()
        store.save("s", session("s", 1))

        assertTrue(store.read("s")!!.contains("\"startedAt\":1"))
    }
}
