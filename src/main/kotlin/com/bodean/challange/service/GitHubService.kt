package com.bodean.challange.service

import com.bodean.challange.client.GitHubClient
import com.bodean.challange.exception.GitHubException
import com.bodean.challange.model.api.BranchInfo
import com.bodean.challange.model.api.RepositoryInfo
import com.bodean.challange.model.branch.Branch
import com.bodean.challange.model.search.repositories.SearchRepositoriesResponse
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class GitHubService(
    private val gitHubClient: GitHubClient,
    private val coroutineScope: CoroutineScope
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun searchRepositoriesByUsername(username: String): SearchRepositoriesResponse {
        return try {
             gitHubClient.searchRepositoriesByUsername(username)
        } catch (webClientResponseException: WebClientResponseException) {
            logger.error("Error for searching repositories for the username :$username ${webClientResponseException.message}")
            val exception =
                if (webClientResponseException.statusCode.is4xxClientError) GitHubException(
                    HttpStatus.NOT_FOUND,
                    "User $username not exist"
                )
                else GitHubException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Cannot get repositories for the user $username"
                )
            throw exception
        }
    }

    suspend fun getRepoBranches(repoName: String): List<Branch> {
        return gitHubClient.getRepoBranches(repoName)
    }

    suspend fun getReposInfosForUsername(username: String): List<RepositoryInfo> {
        logger.info("Get repositories info for the username: $username ")
        val repositoryResponse = searchRepositoriesByUsername(username)
        logger.info("response $repositoryResponse")
       return repositoryResponse.items.chunked(5).map { chunk ->
            chunk.map {
                coroutineScope.async(Dispatchers.IO) {
                    val repositoryName = it.fullName
                    try {
                        val repoBranches = getRepoBranches(repositoryName).map {
                            BranchInfo(
                                name = it.name,
                                lastShaCommit = it.commit.sha
                            )
                        }
                        RepositoryInfo(
                            repositoryName = repositoryName,
                            ownerLogin = it.owner.login,
                            branches = repoBranches
                        )
                    } catch (ex: Exception) {
                        logger.error("Cannot get branches for the repository: $repositoryName ")
                        RepositoryInfo(
                            repositoryName = repositoryName,
                            ownerLogin = it.owner.login,
                            emptyList()
                        )
                    }

                }
            }.awaitAll()
        }.flatten()

    }
}