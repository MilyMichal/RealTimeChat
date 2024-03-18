package com.m.m.RealTimeChat.Configuration;

import com.m.m.RealTimeChat.Services.AppUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppUserDetailService appUserDetailService;

    public SecurityConfig(AppUserDetailService appUserDetailService) {
        this.appUserDetailService = appUserDetailService;
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated())

                .formLogin(form -> form.loginPage("/login")
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .userDetailsService(appUserDetailService)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
