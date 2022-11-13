package com.bodean.challange.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Value("\${github.api.url}")
    private lateinit var gitHubApiUrl: String

    @Bean
    fun webClient(): WebClient{
        return WebClient.builder()
            .baseUrl(gitHubApiUrl).build()
    }
}