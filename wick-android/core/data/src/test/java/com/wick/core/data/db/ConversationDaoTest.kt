package com.wick.core.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ConversationDaoTest {

    private lateinit var db: WickDatabase
    private lateinit var dao: ConversationDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WickDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.conversationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(id: String) = ConversationEntity(
        sessionId = id,
        matchId = "match-$id",
        endsAt = "2099-01-01T00:00:00Z",
        otherUserId = "user-$id",
        otherDisplayName = "User $id",
        otherPhotoUrl = null,
        lastMessage = "Hi",
    )

    @Test
    fun `upsertAll and getAll returns all conversations`() = runTest {
        val convs = listOf(entity("1"), entity("2"), entity("3"))
        dao.upsertAll(convs)

        val result = dao.getAll()
        assertEquals(3, result.size)
    }

    @Test
    fun `upsert replaces existing conversation`() = runTest {
        dao.upsertAll(listOf(entity("1")))
        val updated = entity("1").copy(lastMessage = "Updated")
        dao.upsertAll(listOf(updated))

        val result = dao.getAll()
        assertEquals(1, result.size)
        assertEquals("Updated", result[0].lastMessage)
    }

    @Test
    fun `deleteAll removes all conversations`() = runTest {
        dao.upsertAll(listOf(entity("1"), entity("2")))
        dao.deleteAll()

        val result = dao.getAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAll returns empty list when no data`() = runTest {
        val result = dao.getAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDomain converts entity correctly`() = runTest {
        val e = entity("42")
        dao.upsertAll(listOf(e))

        val domain = dao.getAll().first().toDomain()
        assertEquals("42", domain.sessionId)
        assertEquals("match-42", domain.matchId)
        assertEquals("User 42", domain.otherDisplayName)
    }
}
