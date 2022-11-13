package com.bodean.challange.integration

import com.bodean.challange.config.CoroutineDispatcherConfiguration
import com.bodean.challange.config.ObjectMapperConfiguration
import com.bodean.challange.controller.ExceptionHandler
import com.bodean.challange.integration.ResponseConstants.BRANCH_ENDPOINT_RESPONSE
import com.bodean.challange.integration.ResponseConstants.SEARCH_REPOSITORIES_ENDPOINT_RESPONSE
import com.bodean.challange.model.api.RepositoryInfo
import com.bodean.challange.model.api.error.ErrorResponse
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody


@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient(timeout = "100000")
@WebFluxTest
@ContextConfiguration(classes = [IntegrationTestConfig::class, CoroutineDispatcherConfiguration::class, ObjectMapperConfiguration::class, ExceptionHandler::class])
class GitHubControllerIntegrationTest {

    private lateinit var mockServer: MockWebServer

    @Autowired
    private lateinit var webTestClient: WebTestClient


    @BeforeEach
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start(12346)
    }

    @AfterEach
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun `should return repository name and owner when call to branch api fails`() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path!!
                return if (path.startsWith("/search/repositories")) {
                    MockResponse().setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(SEARCH_REPOSITORIES_ENDPOINT_RESPONSE)
                } else {
                    MockResponse().setResponseCode(500)
                }
            }
        }

        val result = webTestClient.get().uri("/repos/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody<List<RepositoryInfo>>()
            .returnResult().responseBody

        assertNotNull(result)
        assertTrue(result!!.size == 1)
        val responseInfo = result.first()
        assertTrue(responseInfo.branches.isEmpty())
        assertEquals(responseInfo.ownerLogin, "dtrupenn")
        assertEquals(responseInfo.repositoryName, "dtrupenn/Tetris")
    }

    @Test
    fun `should return repository info with branches`() {
        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path!!
                return if (path.startsWith("/search/repositories")) {
                    MockResponse().setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(SEARCH_REPOSITORIES_ENDPOINT_RESPONSE)
                } else if (path.startsWith("/repos/dtrupenn/")) {
                    MockResponse().setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(BRANCH_ENDPOINT_RESPONSE)
                } else {
                    MockResponse().setResponseCode(500)
                }
            }
        }

        val result = webTestClient.get().uri("/repos/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody<List<RepositoryInfo>>()
            .returnResult().responseBody

        assertNotNull(result)
        assertTrue(result!!.size == 1)
        val responseInfo = result.first()
        assertTrue(responseInfo.branches.size == 1)
        val branchInfo = responseInfo.branches.first()
        assertEquals(responseInfo.ownerLogin, "dtrupenn")
        assertEquals(responseInfo.repositoryName, "dtrupenn/Tetris")
        assertEquals(branchInfo.name, "master")
        assertEquals(branchInfo.lastShaCommit, "5e8950737f3d16b310220ad172b301602dd4b2ed")
    }


    @Test
    fun `should fail with 406 error code`() {
        val result = webTestClient.get().uri("/repos/test")
            .accept()
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody<ErrorResponse>()
            .returnResult().responseBody

        assertNotNull(result)
        assertEquals("406", result!!.status)
        assertEquals("Accept header must be application/json", result.message)

    }

    @Test
    fun `should fail with 405 error code`() {

        mockServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path!!
                return if (path.startsWith("/search/repositories")) {
                    MockResponse().setResponseCode(422)
                } else {
                    MockResponse().setResponseCode(500)
                }
            }
        }

        val result = webTestClient.get().uri("/repos/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is4xxClientError
            .expectBody<ErrorResponse>()
            .returnResult().responseBody

        assertNotNull(result)
        assertEquals("404", result!!.status)
        assertEquals("User test not exist", result.message)

    }
}