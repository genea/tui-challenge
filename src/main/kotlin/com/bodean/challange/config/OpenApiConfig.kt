package com.bodean.challange.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource(value = ["classpath:swagger.properties"])
class OpenApiConfig {

    @Bean
    fun gitHubOpenAPI(): OpenAPI {
        return OpenAPI()
            .components(Components())
            .info(
                Info()
                    .title("Git hub challenge API")
                    .description("This is the API documentation for the challenge.")
            )
    }
}