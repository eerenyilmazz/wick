package com.wick.feature.chat

import app.cash.turbine.test
import com.wick.core.data.session.SessionRepository
import com.wick.core.model.AppResult
import com.wick.core.model.ConversationSummary
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: SessionRepository
    private lateinit var viewModel: ConversationsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun conversation(id: String) = ConversationSummary(
        sessionId = id,
        matchId = "match-$id",
        endsAt = "2099-01-01T00:00:00Z",
        otherUserId = "user-$id",
        otherDisplayName = "User $id",
        otherPhotoUrl = null,
        lastMessage = "Hello",
    )

    @Test
    fun `initial load success populates conversations`() = runTest {
        val convs = listOf(conversation("1"), conversation("2"))
        coEvery { repository.listConversations() } returns AppResult.Success(convs)

        viewModel = ConversationsViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.conversations.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial load failure sets error`() = runTest {
        coEvery { repository.listConversations() } returns AppResult.Failure(UiError.NoConnection)

        viewModel = ConversationsViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.conversations.isEmpty())
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry clears error and reloads`() = runTest {
        val convs = listOf(conversation("1"))
        coEvery { repository.listConversations() } returns AppResult.Failure(UiError.Unknown) andThen AppResult.Success(convs)

        viewModel = ConversationsViewModel(repository)
        viewModel.retry()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.conversations.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry calls repository twice`() = runTest {
        coEvery { repository.listConversations() } returns AppResult.Success(emptyList())

        viewModel = ConversationsViewModel(repository)
        viewModel.retry()

        coVerify(exactly = 2) { repository.listConversations() }
    }

    @Test
    fun `empty success shows empty list without error`() = runTest {
        coEvery { repository.listConversations() } returns AppResult.Success(emptyList())

        viewModel = ConversationsViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.conversations.isEmpty())
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
