package com.prewave.supplychaintree.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SupplyChainTreeTestApiSecurity {
    @Bean
    @Order(1)
    fun testApiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            securityMatcher("/test/**")
            cors { disable() }
            csrf { disable() }
            sessionManagement { sessionCreationPolicy = STATELESS }
            authorizeHttpRequests {
                authorize(anyRequest, hasRole("TEST"))
            }
            httpBasic { }
        }

        return http.build()
    }

    @Bean
    @Profile("default")
    fun users(): UserDetailsService {
        val user = User.withUsername("test")
            .password("{noop}secret") // {noop} = no encoding
            .roles("TEST")
            .build()

        return InMemoryUserDetailsManager(user)
    }
}