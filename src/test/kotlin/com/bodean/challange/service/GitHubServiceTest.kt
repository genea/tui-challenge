package com.bodean.challange.service

import com.bodean.challange.client.GitHubClient
import com.bodean.challange.exception.GitHubException
import com.bodean.challange.model.branch.Branch
import com.bodean.challange.model.branch.Commit
import com.bodean.challange.model.search.repositories.RepositoryItem
import com.bodean.challange.model.search.repositories.RepositoryOwner
import com.bodean.challange.model.search.repositories.SearchRepositoriesResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException

@ExtendWith(MockKExtension::class)
class GitHubServiceTest {

    @InjectMockKs
    private lateinit var gitHubService: GitHubService

    @MockK
    private lateinit var gitHubClient: GitHubClient

    private var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { _, _ -> })

    @MockK
    private lateinit var searchRepositoriesResponse: SearchRepositoriesResponse

    @MockK
    private lateinit var branch: Branch
    @MockK
    private lateinit var repositoryItem: RepositoryItem
    @MockK
    private lateinit var repositoryOwner: RepositoryOwner
    @MockK
    private lateinit var commit: Commit
    private val username: String = "test user"
    private val repositoryName = "test full name"
    private val loginName = "test login name"
    private val branchName = "test branch name"
    private val commitSha = "commit sha"


    @Test
    fun `searchRepositoriesByUsername should throw exception with code 404`() {
        val webClientResponseException = WebClientResponseException(422, "somestatus", null, null, null )
        coEvery { gitHubClient.searchRepositoriesByUsername(any()) } throws webClientResponseException
        val exception = assertThrows<GitHubException> {
            runBlocking {
                gitHubService.searchRepositoriesByUsername(username)
            }
        }
        assertEquals(HttpStatus.NOT_FOUND, exception.status)
        assertEquals("User $username not exist", exception.message)
    }

    @Test
    fun `searchRepositoriesByUsername should throw exception with code 500`() {
        val webClientResponseException = WebClientResponseException(500, "somestatus", null, null, null )
        coEvery { gitHubClient.searchRepositoriesByUsername(any()) } throws webClientResponseException
        val exception = assertThrows<GitHubException> {
            runBlocking {
                gitHubService.searchRepositoriesByUsername(username)
            }
        }
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.status)
        assertEquals("Cannot get repositories for the user $username", exception.message)
    }


    @Test
    fun `searchRepositoriesByUsername success`() {
        coEvery { gitHubClient.searchRepositoriesByUsername(any()) } returns searchRepositoriesResponse
        val result = runBlocking { gitHubService.searchRepositoriesByUsername(username) }
        assertNotNull(result)
    }

    @Test
    fun `getRepoBranches success`(){
        coEvery { gitHubClient.getRepoBranches(any()) } returns listOf(branch)
        val result = runBlocking { gitHubService.getRepoBranches("test branch") }
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `getReposInfosForUsername should fail with GitHubException error code 404`(){
        val webClientResponseException = WebClientResponseException(422, "somestatus", null, null, null )
        coEvery { gitHubClient.searchRepositoriesByUsername(any()) } throws webClientResponseException
        val exception = assertThrows<GitHubException> { runBlocking { gitHubService.getReposInfosForUsername(username) } }
        assertEquals(HttpStatus.NOT_FOUND, exception.status)
        assertEquals("User $username not exist", exception.message)
    }

    @Test
    fun `getReposInfosForUsername success when call to all github endpoints success`(){
        coEvery { gitHubClient.searchRepositoriesByUsername(any()) } returns searchRepositoriesResponse
        every { searchRepositoriesResponse.items } returns listOf(repositoryItem)
        every { repositoryItem.owner } returns repositoryOwner
        every { repositoryItem.fullName } returns repositoryName
        every { repositoryOwner.login } returns loginName
        coEvery { gitHubClient.getRepoBranches(any()) } returns listOf(branch)
        every { branch.name } returns branchName
        every { branch.commit } returns commit
        every { commit.sha } returns commitSha

        val result = runBlocking { gitHubService.getReposInfosForUsername(username) }
        assertTrue(result.size == 1)
        val repositoryInfo  =  result[0]
        assertEquals(repositoryInfo.repositoryName, repositoryName)
        val branchInfos = repositoryInfo.branches
        assertTrue(branchInfos.size == 1)
        val branchInfo = branchInfos[0]
        assertEquals(branchInfo.name, branchName)
        assertEquals(branchInfo.lastShaCommit, commitSha)
        assertEquals(repositoryInfo.ownerLogin, loginName)
    }

    @Test
    fun `getReposInfosForUsername success when some call to branch endpoint fails`(){
        val anotherRepositoryItem: RepositoryItem = mockk()
        val anotherRepositoryOwner: RepositoryOwner = mockk()
        val anotherRepositoryName = repositoryName + 1
        val anotherLoginName = loginName + 1
        coEvery { gitHubClient.searchRepositoriesByUsername(any()) } returns searchRepositoriesResponse
        every { searchRepositoriesResponse.items } returns listOf(repositoryItem, anotherRepositoryItem)
        every { repositoryItem.owner } returns repositoryOwner
        every { repositoryItem.fullName } returns repositoryName
        every { repositoryOwner.login } returns loginName
        every { anotherRepositoryItem.owner } returns anotherRepositoryOwner
        every { anotherRepositoryItem.fullName } returns anotherRepositoryName
        every { anotherRepositoryOwner.login } returns anotherLoginName
        coEvery { gitHubClient.getRepoBranches(repositoryName) } returns listOf(branch)
        coEvery { gitHubClient.getRepoBranches(anotherRepositoryName) } throws  WebClientResponseException(500, "somestatus", null, null, null )
        every { branch.name } returns branchName
        every { branch.commit } returns commit
        every { commit.sha } returns commitSha

        val result = runBlocking { gitHubService.getReposInfosForUsername(username) }
        assertTrue(result.size == 2)
        val repositoryInfo  =  result[0]
        assertEquals(repositoryInfo.repositoryName, repositoryName)
        val branchInfos = repositoryInfo.branches
        assertTrue(branchInfos.size == 1)
        val branchInfo = branchInfos[0]
        assertEquals(branchInfo.name, branchName)
        assertEquals(branchInfo.lastShaCommit, commitSha)
        assertEquals(repositoryInfo.ownerLogin, loginName)
        val secondRepositoryInfo = result[1]
        assertEquals(secondRepositoryInfo.repositoryName, anotherRepositoryName)
        assertEquals(secondRepositoryInfo.ownerLogin, anotherLoginName)
        assertTrue(secondRepositoryInfo.branches.isEmpty())
    }
}