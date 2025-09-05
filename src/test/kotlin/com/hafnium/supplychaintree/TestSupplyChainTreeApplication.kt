package com.hafnium.supplychaintree

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<SupplyChainTreeApplication>().with(TestcontainersConfiguration::class).run(*args)
}
