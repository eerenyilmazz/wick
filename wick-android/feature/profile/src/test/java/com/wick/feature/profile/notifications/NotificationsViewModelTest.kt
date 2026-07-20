package com.wick.feature.profile.notifications

import app.cash.turbine.test
import com.wick.core.data.notification.InboxNotificationRepository
import com.wick.core.model.AppResult
import com.wick.core.model.InboxNotification
import com.wick.core.model.UiError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: InboxNotificationRepository
    private lateinit var viewModel: NotificationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun notification(id: String, read: Boolean = false) = InboxNotification(
        id = id,
        type = "MATCH",
        title = "Test $id",
        body = null,
        read = read,
        referenceId = null,
        createdAt = "2025-01-01T00:00:00Z",
    )

    @Test
    fun `initial load success emits items`() = runTest {
        val items = List(5) { notification("$it") }
        coEvery { repository.getInbox(0, 20) } returns AppResult.Success(items)

        viewModel = NotificationsViewModel(repository)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(5, state.items.size)
            assertFalse(state.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial load sets hasMore true when full page returned`() = runTest {
        val items = List(20) { notification("$it") }
        coEvery { repository.getInbox(0, 20) } returns AppResult.Success(items)

        viewModel = NotificationsViewModel(repository)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial load failure sets hasError`() = runTest {
        coEvery { repository.getInbox(0, 20) } returns AppResult.Failure(UiError.NoConnection)

        viewModel = NotificationsViewModel(repository)

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.hasError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMore appends items and increments page`() = runTest {
        val page0 = List(20) { notification("p0-$it") }
        val page1 = List(5) { notification("p1-$it") }
        coEvery { repository.getInbox(0, 20) } returns AppResult.Success(page0)
        coEvery { repository.getInbox(1, 20) } returns AppResult.Success(page1)

        viewModel = NotificationsViewModel(repository)

        viewModel.loadMore()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(25, state.items.size)
            assertFalse(state.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMore is no-op when hasMore is false`() = runTest {
        val items = List(5) { notification("$it") }
        coEvery { repository.getInbox(0, 20) } returns AppResult.Success(items)

        viewModel = NotificationsViewModel(repository)
        viewModel.loadMore()

        coVerify(exactly = 1) { repository.getInbox(any(), any()) }
    }

    @Test
    fun `markAllRead marks all items as read`() = runTest {
        val items = listOf(notification("1", read = false), notification("2", read = false))
        coEvery { repository.getInbox(0, 20) } returns AppResult.Success(items)
        coEvery { repository.markAllRead() } returns AppResult.Success(Unit)

        viewModel = NotificationsViewModel(repository)
        viewModel.markAllRead()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.items.all { it.read })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry resets page and reloads`() = runTest {
        val items = List(3) { notification("$it") }
        coEvery { repository.getInbox(0, 20) } returns AppResult.Failure(UiError.Unknown) andThen AppResult.Success(items)

        viewModel = NotificationsViewModel(repository)
        viewModel.retry()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.hasError)
            assertEquals(3, state.items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
