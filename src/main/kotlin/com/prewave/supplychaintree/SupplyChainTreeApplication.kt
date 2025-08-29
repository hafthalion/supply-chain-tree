package com.prewave.supplychaintree

import com.fasterxml.jackson.databind.ObjectMapper
import com.prewave.supplychaintree.domain.TreeFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SupplyChainTreeApplication {
    @Bean
    fun objectWriter(objectMapper: ObjectMapper) = objectMapper.writer()!!

    @Bean
    fun treeFactory() = TreeFactory()
}

fun main(args: Array<String>) {
    runApplication<SupplyChainTreeApplication>(*args)
}
