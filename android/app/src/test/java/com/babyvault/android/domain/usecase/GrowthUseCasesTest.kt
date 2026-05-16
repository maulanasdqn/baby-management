package com.babyvault.android.domain.usecase

import com.babyvault.android.domain.model.GrowthLog
import com.babyvault.android.domain.repo.GrowthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GrowthUseCasesTest {

    private val now = Instant.now()

    private fun log(id: String, weight: Int? = null, height: Int? = null) = GrowthLog(
        id = id,
        weightGrams = weight,
        heightMm = height,
        notes = "",
        loggedAt = now,
    )

    private inner class FakeRepo(
        private val items: MutableList<GrowthLog> = mutableListOf(),
    ) : GrowthRepository {
        override suspend fun log(weightGrams: Int?, heightMm: Int?, notes: String, loggedAt: Instant): Result<GrowthLog> {
            val g = GrowthLog("id-${items.size}", weightGrams, heightMm, notes, loggedAt)
            items.add(g)
            return Result.success(g)
        }
        override suspend fun listByRange(from: Instant, to: Instant): Result<List<GrowthLog>> =
            Result.success(items.filter { it.loggedAt >= from && it.loggedAt <= to })
    }

    // --- LogGrowthUseCase ---

    @Test
    fun `log with weight only returns success`() = runTest {
        val result = LogGrowthUseCase(FakeRepo())(weightGrams = 3500, heightMm = null, notes = "")
        assertTrue(result.isSuccess)
        assertEquals(3500, result.getOrThrow().weightGrams)
    }

    @Test
    fun `log with height only returns success`() = runTest {
        val result = LogGrowthUseCase(FakeRepo())(weightGrams = null, heightMm = 520, notes = "check")
        assertTrue(result.isSuccess)
        assertEquals(520, result.getOrThrow().heightMm)
    }

    @Test
    fun `log with both measurements returns success`() = runTest {
        val result = LogGrowthUseCase(FakeRepo())(weightGrams = 4000, heightMm = 560, notes = "")
        val log = result.getOrThrow()
        assertEquals(4000, log.weightGrams)
        assertEquals(560, log.heightMm)
    }

    @Test
    fun `log delegates notes to repo`() = runTest {
        val result = LogGrowthUseCase(FakeRepo())(weightGrams = 3000, heightMm = null, notes = "healthy")
        assertEquals("healthy", result.getOrThrow().notes)
    }

    // --- ListGrowthByRangeUseCase ---

    @Test
    fun `list by range returns empty for empty repo`() = runTest {
        val result = ListGrowthByRangeUseCase(FakeRepo())(
            from = now.minusSeconds(3600),
            to = now,
        )
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `list by range returns items within range`() = runTest {
        val repo = FakeRepo(mutableListOf(
            GrowthLog("1", 3000, null, "", now.minusSeconds(100)),
            GrowthLog("2", 3100, null, "", now.minusSeconds(7200)),
        ))
        val result = ListGrowthByRangeUseCase(repo)(
            from = now.minusSeconds(3600),
            to = now,
        )
        val items = result.getOrThrow()
        assertEquals(1, items.size)
        assertEquals("1", items[0].id)
    }
}
