package com.portfolio.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers("/api/v1/auth/**", "/api/v1/projects/**", "/api/v1/blogs/**", "/api/v1/skills/**",
								"/api/v1/contact/**", "/actuator/**")
						.permitAll().pathMatchers(HttpMethod.GET, "/api/v1/projects/**").permitAll()
						.pathMatchers(HttpMethod.GET, "/api/v1/blogs/**").permitAll()
						.pathMatchers(HttpMethod.GET, "/api/v1/skills/**").permitAll()
						.pathMatchers(HttpMethod.POST, "/api/v1/contact/**").permitAll()
						.pathMatchers("/api/v1/auth/**", "/api/v1/admin/**").permitAll().anyExchange().authenticated())
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable);

		return http.build();
	}
}
