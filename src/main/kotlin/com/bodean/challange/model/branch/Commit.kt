package com.bodean.challange.model.branch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Commit(
    val sha: String,
    val url: String
)
