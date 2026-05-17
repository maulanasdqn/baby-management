package com.babyvault.android.domain.usecase
import com.babyvault.android.domain.model.Milestone
import com.babyvault.android.domain.repo.MilestoneRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
class MilestoneUseCasesTest {
    private val now = Instant.now()
    private fun milestone(id: String, title: String) = Milestone(
        id = id,
        title = title,
        description = "",
        occurredAt = now.minusSeconds(86_400),
        createdAt = now,
    )
    private inner class FakeRepo(
        private val items: MutableList<Milestone> = mutableListOf(),
    ) : MilestoneRepository {
        override suspend fun create(title: String, description: String, occurredAt: Instant): Result<Milestone> {
            val m = milestone("id-${items.size}", title)
            items.add(m)
            return Result.success(m)
        }
        override suspend fun list(limit: Int, offset: Int): Result<List<Milestone>> =
            Result.success(items.drop(offset).take(limit))
        override suspend fun getById(id: String): Result<Milestone> =
            items.find { it.id == id }?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("not found"))
        override suspend fun delete(id: String): Result<Unit> {
            items.removeIf { it.id == id }
            return Result.success(Unit)
        }
    }
    @Test
    fun `create delegates to repo and returns milestone`() = runTest {
        val repo = FakeRepo()
        val result = CreateMilestoneUseCase(repo)("First Steps", "Baby walked!", now.minusSeconds(3600))
        assertTrue(result.isSuccess)
        assertEquals("First Steps", result.getOrThrow().title)
    }
    @Test
    fun `create adds item to repo`() = runTest {
        val repo = FakeRepo()
        CreateMilestoneUseCase(repo)("Smile", "", now.minusSeconds(3600))
        val listed = ListMilestonesUseCase(repo)().getOrThrow()
        assertEquals(1, listed.size)
        assertEquals("Smile", listed[0].title)
    }
    @Test
    fun `list returns empty when repo is empty`() = runTest {
        val result = ListMilestonesUseCase(FakeRepo())()
        assertTrue(result.getOrThrow().isEmpty())
    }
    @Test
    fun `list respects limit`() = runTest {
        val repo = FakeRepo(mutableListOf(
            milestone("1", "A"), milestone("2", "B"), milestone("3", "C"),
        ))
        val result = ListMilestonesUseCase(repo)(limit = 2)
        assertEquals(2, result.getOrThrow().size)
    }
    @Test
    fun `list uses default limit of 20`() = runTest {
        val items = (1..25).map { milestone("$it", "M$it") }.toMutableList()
        val repo = FakeRepo(items)
        val result = ListMilestonesUseCase(repo)()
        assertEquals(20, result.getOrThrow().size)
    }
    @Test
    fun `delete removes milestone from repo`() = runTest {
        val repo = FakeRepo(mutableListOf(milestone("abc", "Crawling")))
        DeleteMilestoneUseCase(repo)("abc")
        val listed = ListMilestonesUseCase(repo)().getOrThrow()
        assertTrue(listed.isEmpty())
    }
    @Test
    fun `delete returns success`() = runTest {
        val repo = FakeRepo(mutableListOf(milestone("xyz", "Bath")))
        val result = DeleteMilestoneUseCase(repo)("xyz")
        assertTrue(result.isSuccess)
    }
}
