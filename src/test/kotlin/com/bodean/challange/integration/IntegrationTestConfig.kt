package com.bodean.challange.integration

import com.bodean.challange.client.GitHubClient
import com.bodean.challange.controller.GitHubController
import com.bodean.challange.service.GitHubService
import kotlinx.coroutines.CoroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@TestConfiguration
@PropertySource("classpath:application.properties")
class IntegrationTestConfig {

    @Value("\${github.api.url}")
    private lateinit var gitHubApiUrl: String

    @Bean
    fun webClient(): WebClient {
        val client = HttpClient.create().responseTimeout(Duration.ofSeconds(10L))
        return WebClient.builder()
            .baseUrl(gitHubApiUrl).clientConnector(ReactorClientHttpConnector(client)).build()
    }

    @Bean
    fun gitHubClient(webClient: WebClient): GitHubClient {
        return GitHubClient(webClient)
    }

    @Bean
    fun gitHubService(gitHubClient: GitHubClient, coroutineScope: CoroutineScope): GitHubService {
        return GitHubService(gitHubClient, coroutineScope)
    }

    @Bean
    fun gitHubController(gitHubService: GitHubService): GitHubController {
        return GitHubController(gitHubService)
    }
}