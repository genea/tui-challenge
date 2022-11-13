package com.bodean.challange.model.api

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Model for repository information")
data class RepositoryInfo(
    @field:Schema(
        description = "Repository name",
        example = "dtrupenn/Tetris"
    )
    val repositoryName: String,
    @field:Schema(description = "owner login")
    val ownerLogin: String,
    @field:Schema(description = "Branches information")
    val branches: List<BranchInfo>
)