package com.bank.common.config;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Profile("!test")
@Configuration
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value("${spring.webflux.base-path}")
    private String contextPath;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return Mono::just;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(securityContextRepository())
                .authorizeExchange(ex -> ex
                        .pathMatchers("/", "/register", "/favicon.ico").permitAll()
                        .pathMatchers(HttpMethod.GET, "/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/login").permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((exchange, ex) ->
                                Mono.fromRunnable(() -> {
                                    ServerHttpResponse res = exchange.getResponse();
                                    res.setStatusCode(HttpStatus.SEE_OTHER);
                                    res.getHeaders().setLocation(URI.create(contextPath + "/login"));
                                })
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(((exchange, authentication) ->
                                        exchange.getExchange().getSession()
                                                .flatMap(WebSession::invalidate)
                                                .then(Mono.fromRunnable(() -> {
                                                    ServerHttpResponse response = exchange.getExchange().getResponse();
                                                    response.getCookies().clear();

                                                    ResponseCookie expiredCookie = ResponseCookie.from("SESSION", "")
                                                            .path("/")
                                                            .maxAge(0)
                                                            .build();
                                                    response.addCookie(expiredCookie);
                                                    response.setStatusCode(HttpStatus.FOUND);
                                                    response.getHeaders().setLocation(URI.create(contextPath + "/login"));
                                                }))
                                )
                        )
                )
                .build();
    }
}
