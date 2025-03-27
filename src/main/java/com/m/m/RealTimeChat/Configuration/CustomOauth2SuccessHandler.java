package com.m.m.RealTimeChat.Configuration;

import com.m.m.RealTimeChat.Models.User;
import com.m.m.RealTimeChat.Services.UserStorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;


import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    UserStorageService userStorageService;

    @Autowired
    PasswordEncoder passwordEncoder;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {


        if (authentication instanceof OAuth2AuthenticationToken token) {
            System.out.println("DEBUG ONAUTHENTICATION OAUTH2 SUCCES");
            System.out.println("DEBUG ATTRIBUTES: \n name = " + Objects.requireNonNull(token.getPrincipal().getAttribute("name")));
            System.out.println("DEBUG ATTRIBUTES: \n nickname = " + Objects.requireNonNull(token.getPrincipal().getAttribute("given_name")));
            System.out.println("DEBUG ATTRIBUTES: \n mail = " + Objects.requireNonNull(token.getPrincipal().getAttribute("email")));
            System.out.println("DEBUG ATTRIBUTES: \n principal.getName() = " + token.getPrincipal().getName());
            Optional<User> existingUser = userStorageService.findUser(token.getPrincipal().getAttribute("email"));

            if (existingUser.isEmpty()) {
                Map<String, Object> attributes = token.getPrincipal().getAttributes();

                User newOauth2User = new User();
                newOauth2User.setProfilePic(attributes.get("picture").toString());
                newOauth2User.setRoles("user");
                newOauth2User.setNickname(attributes.get("given_name").toString().replaceAll(" ", "_"));
                newOauth2User.setUserName(token.getPrincipal().getName());
                newOauth2User.setEmail(attributes.get("email").toString());
                newOauth2User.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                userStorageService.saveUserToStorage(newOauth2User);
                System.out.println("OAUTH2 USER CREATED");


            }
        }
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            System.out.println("DEBUG: CACHE DELETED");
            requestCache.removeRequest(request, response);
        }
        //redirectStrategy.sendRedirect(request, response, "/chat/oauth2/google");
        redirectStrategy.sendRedirect(request, response, "/chat");
    }
}

