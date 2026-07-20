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
class NotificationDaoTest {

    private lateinit var db: WickDatabase
    private lateinit var dao: NotificationDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WickDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.notificationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(id: String, createdAt: String = "2025-01-01T00:00:0${id}Z") = NotificationEntity(
        id = id,
        type = "MATCH",
        title = "Title $id",
        body = null,
        read = false,
        referenceId = null,
        createdAt = createdAt,
    )

    @Test
    fun `upsertAll and getPage returns correct page`() = runTest {
        val notifs = (1..25).map { entity("$it", "2025-01-01T00:00:%02dZ".format(it)) }
        dao.upsertAll(notifs)

        val page0 = dao.getPage(limit = 20, offset = 0)
        assertEquals(20, page0.size)

        val page1 = dao.getPage(limit = 20, offset = 20)
        assertEquals(5, page1.size)
    }

    @Test
    fun `upsert replaces existing notification`() = runTest {
        dao.upsertAll(listOf(entity("1")))
        val updated = entity("1").copy(read = true)
        dao.upsertAll(listOf(updated))

        val result = dao.getPage(limit = 10, offset = 0)
        assertEquals(1, result.size)
        assertTrue(result[0].read)
    }

    @Test
    fun `deleteAll clears all notifications`() = runTest {
        dao.upsertAll(listOf(entity("1"), entity("2")))
        dao.deleteAll()

        val result = dao.getPage(limit = 10, offset = 0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getPage returns empty list beyond data`() = runTest {
        dao.upsertAll(listOf(entity("1")))

        val result = dao.getPage(limit = 20, offset = 10)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDomain converts entity correctly`() = runTest {
        val e = entity("99")
        dao.upsertAll(listOf(e))

        val domain = dao.getPage(limit = 1, offset = 0).first().toDomain()
        assertEquals("99", domain.id)
        assertEquals("MATCH", domain.type)
        assertEquals("Title 99", domain.title)
    }
}
