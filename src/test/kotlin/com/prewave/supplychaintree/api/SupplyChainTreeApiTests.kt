package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.TestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class SupplyChainTreeApiTests(
    @param:Autowired val rest: TestRestTemplate,
) {

    @Test
    fun `should create edge`() {
        val entity = rest.postForEntity("/api/edge/from/1/to/2", null, Any::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `should not create duplicate edge`() {
        rest.postForEntity("/api/edge/from/1/to/3", null, Any::class.java)

        val entity = rest.postForEntity("/api/edge/from/1/to/3", null, Any::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    @Disabled //TODO Enable and extend when implemented
    fun `should delete edge`() {
        val entity = rest.exchange("/api/edge/from/1/to/2", HttpMethod.DELETE, null, Any::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @Disabled //TODO Enable and extend when implemented
    fun `should get tree`() {
        val entity = rest.getForEntity("/api/tree/from/1", Any::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    }
}