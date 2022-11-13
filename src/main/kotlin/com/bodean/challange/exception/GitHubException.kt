package com.bodean.challange.exception

import org.springframework.http.HttpStatus

class GitHubException(val status: HttpStatus, override val message: String) : RuntimeException(message) {
}