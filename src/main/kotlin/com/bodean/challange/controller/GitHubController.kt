package com.bodean.challange.controller

import com.bodean.challange.exception.GitHubException
import com.bodean.challange.model.api.RepositoryInfo
import com.bodean.challange.model.api.error.ErrorResponse
import com.bodean.challange.service.GitHubService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class GitHubController(private val gitHubService: GitHubService) {

    @Operation(
        summary = "Get repositories information for the specified user",
        description = "Returns repository name, owner login, for each branch it’s name and last commit sha"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Information fetched with success",
                content = [Content(
                    mediaType = "application/json", array = ArraySchema(
                        schema = Schema(
                            implementation = RepositoryInfo::class
                        )
                    )
                )]
            ),
            ApiResponse(
                responseCode = "406",
                description = "Http accept header is not valid",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "405",
                description = "User not found",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @GetMapping("/repos/{username}")
    suspend fun getRepositoryInfoByUsername(
        @RequestHeader(
            name = "Accept",
            required = true
        ) acceptHeader: String, @PathVariable username: String
    ): List<RepositoryInfo> {
        validateAcceptHeader(acceptHeader)
        return gitHubService.getReposInfosForUsername(username)
    }

    private fun validateAcceptHeader(header: String) {
        if (header != MediaType.APPLICATION_JSON_VALUE) throw GitHubException(
            HttpStatus.NOT_ACCEPTABLE,
            "Accept header must be application/json"
        )
    }
}