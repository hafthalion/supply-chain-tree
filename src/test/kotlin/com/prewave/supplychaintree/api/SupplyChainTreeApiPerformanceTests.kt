package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.TestcontainersConfiguration
import com.prewave.supplychaintree.repository.SupplyChainTreeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.util.StreamUtils.drain
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import kotlin.time.DurationUnit
import kotlin.time.measureTime

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class SupplyChainTreeApiPerformanceTests(
    @param:Autowired val rest: TestRestTemplate,
) {
    companion object {
        @BeforeAll
        @JvmStatic
        fun createLargeTree(@Autowired repository: SupplyChainTreeRepository) {
            val duration = measureTime { repository.createLargeTestTree(100, 1_000_000) }

            println("Setup duration: " + duration.toString(DurationUnit.SECONDS, 2))
        }
    }

    @RepeatedTest(3)
    fun `should fetch large tree`() {
        val duration = measureTime {
            val requestCallback = RequestCallback { it.headers.accept = listOf(APPLICATION_JSON) }
//            val responseExtractor = ResponseExtractor { it.body.reader().readText() }
            val responseExtractor = ResponseExtractor { drain(it.body.buffered()) }
            val response = rest.execute("/api/tree/from/100", GET, requestCallback, responseExtractor)

            println("Response: $response")
            assertThat(response).isNotNull
        }

        println("Test duration: " + duration.toString(DurationUnit.SECONDS, 2))
    }
}
