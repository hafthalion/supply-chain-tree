package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.TestcontainersConfiguration
import com.prewave.supplychaintree.domain.TreeEdge
import com.prewave.supplychaintree.domain.exception.TreeNotFoundException
import com.prewave.supplychaintree.service.SupplyChainTreeRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.test.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class SupplyChainTreeApiTests @Autowired constructor(
    private val rest: TestRestTemplate,
    private val repository: SupplyChainTreeRepository,
) {
    @Test
    fun `should create new edge`() {
        val entity = rest.postForEntity("/api/edge/from/10/to/11", null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(repository.fetchTree(10).edges).hasSize(1)
    }

    @Test
    fun `should not create duplicate edge`() {
        repository.createEdge(TreeEdge(20, 21))

        val entity = rest.postForEntity("/api/edge/from/20/to/21", null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(repository.fetchTree(20).edges).hasSize(1)
    }

    @Test
    fun `should delete existing edge`() {
        repository.createEdge(TreeEdge(30, 31))

        val entity = rest.exchange("/api/edge/from/30/to/31", HttpMethod.DELETE, null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThatThrownBy {
            repository.fetchTree(30)
        }.isInstanceOf(TreeNotFoundException::class.java)
    }

    @Test
    fun `should fail when deleting missing edge`() {
        val entity = rest.exchange("/api/edge/from/30/to/32", HttpMethod.DELETE, null, Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should fetch tree`() {
        repository.createEdge(TreeEdge(40, 41))
        repository.createEdge(TreeEdge(40, 42))
        repository.createEdge(TreeEdge(41, 43))
        repository.createEdge(TreeEdge(41, 44))

        val response = rest.getForEntity("/api/tree/from/40", List::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).hasSize(2).first()
            .hasFieldOrPropertyWithValue("id", 40)
            .hasFieldOrPropertyWithValue("to", listOf(41, 42))
    }

    @Test
    fun `should fail when fetching missing tree`() {
        val entity = rest.getForEntity("/api/tree/from/45", Any::class.java)

        assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}
