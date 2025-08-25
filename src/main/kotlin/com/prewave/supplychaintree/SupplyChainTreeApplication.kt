package com.prewave.supplychaintree

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SupplyChainTreeApplication {
    @Bean
    fun objectWriter(objectMapper: ObjectMapper): ObjectWriter = objectMapper.writer()
}

fun main(args: Array<String>) {
    runApplication<SupplyChainTreeApplication>(*args)
}
