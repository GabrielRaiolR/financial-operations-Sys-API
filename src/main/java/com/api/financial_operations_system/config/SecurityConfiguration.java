package com.api.financial_operations_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticação HTTP Basic para desenvolvimento/testes via Postman.
 * A senha em texto plano é prefixada com {noop} aqui no código, evitando ambiguidade do
 * DelegatingPasswordEncoder com spring.security.user.password apenas no properties.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${APP_SECURITY_USER:admin}") String username,
            @Value("${APP_SECURITY_PASSWORD:finops_pass}") String rawPassword) {
        UserDetails user = User.withUsername(username)
                .password("{noop}" + rawPassword)
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
