package com.bodean.challange.model.api.error

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Error response")
data class ErrorResponse(
    @field:Schema(description = "Error status", example = "405")
    val status: String,
    @field: Schema(description = "Error message", example = "User test not exist")
    val message: String
)
