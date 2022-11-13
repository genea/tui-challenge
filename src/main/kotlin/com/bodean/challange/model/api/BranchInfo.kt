package com.bodean.challange.model.api

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Branch information")
data class BranchInfo(
    @field:Schema(description = "Branch name")
    val name: String,
    @field:Schema(description = "Last sha commit")
    val lastShaCommit: String,
)
