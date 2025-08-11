package com.prewave.supplychaintree.api

/*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.SecurityFilterChain
*/

//TODO Matching doesn't work correctly yet
//@Configuration
//@EnableWebSecurity
class SupplyChainTreeApiSecurity {
    /*@Bean
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/api/ **", permitAll)
                authorize("/test/ **", permitAll)
                authorize("/swagger-ui/ **", permitAll)
                authorize("/v3/api-docs/ **", permitAll)
                authorize(anyRequest, denyAll)
            }
            sessionManagement {
                sessionCreationPolicy = STATELESS
            }
            cors {
                disable()
            }
        }

        return http.build()
    }*/
}