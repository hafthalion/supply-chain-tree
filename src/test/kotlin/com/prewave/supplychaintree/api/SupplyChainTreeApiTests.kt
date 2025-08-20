package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.TestcontainersConfiguration
import com.prewave.supplychaintree.service.SupplyChainTreeRepository
import org.assertj.core.api.Assertions.assertThat
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
    @param:Autowired val repository: SupplyChainTreeRepository,
) {

    @Test
    fun `should create new edge`() {
        val entity = rest.postForEntity("/api/edge/from/10/to/11", null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(repository.fetchReachableEdges(10)).hasSize(1)
    }

    @Test
    fun `should not create duplicate edge`() {
        repository.createEdge(20, 21)

        val entity = rest.postForEntity("/api/edge/from/20/to/21", null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(repository.fetchReachableEdges(20)).hasSize(1)
    }

    @Test
    fun `should delete existing edge`() {
        repository.createEdge(30, 31)

        val entity = rest.exchange("/api/edge/from/30/to/31", HttpMethod.DELETE, null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(repository.fetchReachableEdges(30)).isEmpty()
    }

    @Test
    fun `should fail when deleting missing edge`() {
        val entity = rest.exchange("/api/edge/from/30/to/32", HttpMethod.DELETE, null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should fetch tree`() {
        repository.createEdge(40, 41)
        repository.createEdge(40, 42)
        repository.createEdge(41, 43)
        repository.createEdge(41, 44)

        val entity = rest.getForEntity("/api/tree/from/40", List::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).hasSize(2).first()
            .hasFieldOrPropertyWithValue("id", 40)
            .hasFieldOrPropertyWithValue("to", listOf(41, 42))
    }

    @Test
    fun `should fail when fetching missing tree`() {
        val entity = rest.getForEntity("/api/tree/from/45", Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}
