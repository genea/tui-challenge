package com.bodean.challange.config

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineDispatcherConfiguration {

    @Bean
    fun coroutineScope(): CoroutineScope{
        return CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { _, _ -> })
    }
}