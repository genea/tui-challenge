package com.bodean.challange.controller

import com.bodean.challange.exception.GitHubException
import com.bodean.challange.model.api.error.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(GitHubException::class)
    fun handleGitHubException(gitHubException: GitHubException): ResponseEntity<Any> {
        val status = gitHubException.status
        return ResponseEntity.status(status)
            .body(ErrorResponse(status = "${status.value()}", message = gitHubException.message))
    }
}