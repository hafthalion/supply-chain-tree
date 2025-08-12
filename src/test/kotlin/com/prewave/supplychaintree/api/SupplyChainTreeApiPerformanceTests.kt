package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.TestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepeatedTest.CURRENT_REPETITION_PLACEHOLDER
import org.junit.jupiter.api.RepeatedTest.TOTAL_REPETITIONS_PLACEHOLDER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.util.StreamUtils.drain
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import kotlin.time.measureTime

/**
 * Performance test used to assess the total performance for large trees.
 * Change the [treeSize] to generate a test trees another sizes.
 * Also modify the response time assumption [responseTimeMs].
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SupplyChainTreeApiPerformanceTests(
    @param:Autowired val rest: TestRestTemplate,
) {
    private val treeSize = 1_000_000
    private val responseTimeMs = 3000
    private val responseSizeFactor = 9

    @Test
    @Order(1)
    fun `should create large tree`() {
        val entity = rest.postForEntity("/test/tree/from/100?size=$treeSize", null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @Order(2)
    fun `should fetch large tree for the first time`() {
        fetchLargeTree()
    }

    @RepeatedTest(3, name = "{displayName} $CURRENT_REPETITION_PLACEHOLDER of $TOTAL_REPETITIONS_PLACEHOLDER")
    @Order(3)
    fun `should fetch large tree in time after warm-up`() {
        val duration = measureTime { fetchLargeTree() }.inWholeMilliseconds

        assumeTrue(duration < responseTimeMs.toLong()) { "Test was not completed in max time of $responseTimeMs ms, took $duration ms" }
    }

    private fun fetchLargeTree() {
        val requestCallback = RequestCallback { it.headers.accept = listOf(APPLICATION_JSON) }
        val responseExtractor = ResponseExtractor { it.statusCode to drain(it.body.buffered()) }
        val response = rest.execute("/api/tree/from/100", GET, requestCallback, responseExtractor)

        assertThat(response.first).isEqualTo(HttpStatus.OK)
        assertThat(response.second).isGreaterThan(treeSize * responseSizeFactor)
    }
}
