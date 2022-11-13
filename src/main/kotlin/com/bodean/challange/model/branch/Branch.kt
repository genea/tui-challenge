package com.bodean.challange.model.branch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Branch(
    val name: String,
    val protected : Boolean,
    val commit: Commit
)
