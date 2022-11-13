package com.bodean.challange.model.search.repositories

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class RepositoryItem(
    val id: Int,
    val gitUrl: String,
    val fullName: String,
    val owner: RepositoryOwner
)
