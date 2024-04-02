package com.m.m.RealTimeChat.Configuration;

import com.m.m.RealTimeChat.Services.AppUserDetailService;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final AppUserDetailService appUserDetailService;

    public SecurityConfig(CustomAuthenticationProvider customAuthenticationProvider, AppUserDetailService appUserDetailService) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.appUserDetailService = appUserDetailService;

    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/register", "/sessionError").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.loginPage("/login")
                        .permitAll()


                        .failureUrl("/login-error")
                )

                .authenticationProvider(customAuthenticationProvider)
                .userDetailsService(appUserDetailService)
                /*.sessionManagement(session -> session
                        //.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/sessionError"))*/

                .logout(logout -> logout.logoutSuccessUrl("/").permitAll()
                        .deleteCookies("JSESSIONID").invalidateHttpSession(true))

                /*.csrf(AbstractHttpConfigurer::disable)*/

                .build();
    }


    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
