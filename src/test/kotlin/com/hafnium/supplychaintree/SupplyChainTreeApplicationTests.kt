package com.hafnium.supplychaintree

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class SupplyChainTreeApplicationTests {
    @Test
    fun `spring context loads`() {
    }
}
