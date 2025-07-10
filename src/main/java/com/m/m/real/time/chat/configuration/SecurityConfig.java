package com.m.m.real.time.chat.configuration;

import com.m.m.real.time.chat.services.AppUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.HashSet;
import java.util.Set;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;

    private final AppUserDetailService appUserDetailService;

    private final SimpMessagingTemplate messagingTemplates;


    @Autowired
    public SecurityConfig(CustomAuthenticationProvider customAuthenticationProvider, AppUserDetailService appUserDetailService, SimpMessagingTemplate messagingTemplates) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.appUserDetailService = appUserDetailService;
        this.messagingTemplates = messagingTemplates;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/Images/**", "/CSS/**", "/register", "/logout", "/ProfilePic/**", "/error","/passReset/**","/JS/**","/passReset/{uuid}/**").permitAll()
                        .requestMatchers("/history/**").hasAnyRole("ADMIN", "user")
                        .anyRequest().authenticated()

                )
                .oauth2Login(oauth2 -> oauth2.loginPage("/login")
                        .permitAll()
                        .failureUrl("/login-error")
                        .successHandler(customOauth2SuccessHandler())
                        .userInfoEndpoint(info -> info.userAuthoritiesMapper(userAuthoritiesMapper()))
                )

                .formLogin(form -> form.loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/chat", true)
                        .failureUrl("/login-error")
                )
                .authenticationProvider(customAuthenticationProvider)
                .userDetailsService(appUserDetailService)
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll()

                        .addLogoutHandler(customLogoutHandler())
                        /*.deleteCookies("JSESSIONID").invalidateHttpSession(true)*/


                )

               // .anonymous(AnonymousConfigurer::disable)
                .csrf(Customizer.withDefaults())
                .build();
    }


    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }

    @Bean
    public LogoutHandler customLogoutHandler() {
        return new CustomLogoutHandler(messagingTemplates);
    }

    @Bean
    public AuthenticationSuccessHandler customOauth2SuccessHandler() {
        return new CustomOauth2SuccessHandler();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals("OIDC_USER")) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_user"));
                } else {
                    mappedAuthorities.add(authority);
                }
            }
            return mappedAuthorities;
        };
    }
}
