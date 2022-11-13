package com.bodean.challange.client

import com.bodean.challange.model.branch.Branch
import com.bodean.challange.model.search.repositories.SearchRepositoriesResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Component
class GitHubClient(private val webClient: WebClient) {

    @Value("\${github.personal.access.token}")
    private lateinit var gitHubPersonalAccessToken: String

    suspend fun getRepoBranches(repoName: String): List<Branch> {
        return webClient.get().uri("repos/$repoName/branches")
            .headers { it.setBearerAuth(gitHubPersonalAccessToken) }.retrieve().awaitBody()
    }

    suspend fun searchRepositoriesByUsername(username: String): SearchRepositoriesResponse {
        return webClient.get()
            .uri {
                it.path("/search/repositories").queryParam("q", "user:$username").build()
                    .normalize()
            }
            .headers { it.setBearerAuth(gitHubPersonalAccessToken) }
            .retrieve()
            .awaitBody()
    }

}