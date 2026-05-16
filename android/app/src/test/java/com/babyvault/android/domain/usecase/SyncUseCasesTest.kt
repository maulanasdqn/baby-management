package com.babyvault.android.domain.usecase

import com.babyvault.android.domain.model.SyncStatus
import com.babyvault.android.domain.repo.SyncRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class SyncUseCasesTest {

    private val configured = SyncStatus(0, 0, 0, Instant.ofEpochMilli(1_000_000), isConfigured = true)
    private val unconfigured = SyncStatus(2, 1, 0, null, isConfigured = false)

    private inner class FakeRepo(
        private var status: SyncStatus = unconfigured,
        private var configureResult: Result<Unit> = Result.success(Unit),
        private var syncResult: Result<SyncStatus> = Result.success(configured),
    ) : SyncRepository {
        var configureCallCount = 0
        var syncCallCount = 0
        var lastUrl = ""
        var lastKey = ""

        override suspend fun configureSyncServer(serverUrl: String, apiKey: String): Result<Unit> {
            configureCallCount++
            lastUrl = serverUrl
            lastKey = apiKey
            return configureResult
        }
        override suspend fun syncNow(): Result<SyncStatus> {
            syncCallCount++
            return syncResult
        }
        override suspend fun getStatus(): Result<SyncStatus> = Result.success(status)
    }

    // --- ConfigureSyncServerUseCase ---

    @Test
    fun `configure delegates url and key to repo`() = runTest {
        val repo = FakeRepo()
        ConfigureSyncServerUseCase(repo)("https://sync.example.com", "mykey")
        assertEquals("https://sync.example.com", repo.lastUrl)
        assertEquals("mykey", repo.lastKey)
    }

    @Test
    fun `configure returns success when repo succeeds`() = runTest {
        val result = ConfigureSyncServerUseCase(FakeRepo())("https://sync.example.com", "key")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `configure propagates repo failure`() = runTest {
        val repo = FakeRepo(configureResult = Result.failure(RuntimeException("network error")))
        val result = ConfigureSyncServerUseCase(repo)("url", "key")
        assertTrue(result.isFailure)
    }

    // --- SyncNowUseCase ---

    @Test
    fun `sync now delegates to repo`() = runTest {
        val repo = FakeRepo()
        SyncNowUseCase(repo)()
        assertEquals(1, repo.syncCallCount)
    }

    @Test
    fun `sync now returns repo status on success`() = runTest {
        val repo = FakeRepo(syncResult = Result.success(configured))
        val result = SyncNowUseCase(repo)()
        assertEquals(configured, result.getOrThrow())
    }

    @Test
    fun `sync now propagates repo failure`() = runTest {
        val repo = FakeRepo(syncResult = Result.failure(RuntimeException("timeout")))
        val result = SyncNowUseCase(repo)()
        assertTrue(result.isFailure)
    }

    // --- GetSyncStatusUseCase ---

    @Test
    fun `get status returns current repo status`() = runTest {
        val repo = FakeRepo(status = unconfigured)
        val result = GetSyncStatusUseCase(repo)()
        assertEquals(unconfigured, result.getOrThrow())
    }

    @Test
    fun `get status reflects pending counts`() = runTest {
        val pending = SyncStatus(3, 2, 1, null, true)
        val repo = FakeRepo(status = pending)
        val status = GetSyncStatusUseCase(repo)().getOrThrow()
        assertEquals(6, status.totalPending)
    }
}
