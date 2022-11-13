package com.bodean.challange.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectMapperConfiguration {
    fun objectMapper(): ObjectMapper{
        return ObjectMapper().registerKotlinModule().apply { JavaTimeModule() }
    }
}