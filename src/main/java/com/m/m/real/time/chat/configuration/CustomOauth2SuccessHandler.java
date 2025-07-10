package com.m.m.real.time.chat.configuration;

import com.m.m.real.time.chat.models.User;
import com.m.m.real.time.chat.services.UserStorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;


import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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

        //check if was logged in basic user or Oauth2 user
        if (authentication instanceof OAuth2AuthenticationToken token) {

            // check if logged user is already registered in DB
            Optional<User> existingUser = userStorageService.findUser(token.getPrincipal().getAttribute("email"));

            //If user is not registered, new acc is created
            if (existingUser.isEmpty()) {
                Map<String, Object> attributes = token.getPrincipal().getAttributes();

                User newOauth2User = new User();
                newOauth2User.setProfilePic(attributes.get("picture").toString());
                newOauth2User.setRoles("user");
                newOauth2User.setNickname(attributes.get("given_name").toString().replace(" ", "_"));
                newOauth2User.setUserName(token.getPrincipal().getName());
                newOauth2User.setEmail(attributes.get("email").toString());
                newOauth2User.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                userStorageService.saveUserToStorage(newOauth2User);

            }
            //check if already registered user is banned
            if (existingUser.isPresent() && !existingUser.get().isNonBanned()) {
                throw new LockedException(String.format(
                        "Your account is BANNED! <br> BAN will expire on: %s", existingUser.get()
                                .getBanExpiration()
                                .format(DateTimeFormatter.ofPattern("dd-M-yyyy H:mm:ss"))));
            }
        }
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            requestCache.removeRequest(request, response);
        }
        //redirection to chet page when authentication is valid
        redirectStrategy.sendRedirect(request, response, "/chat");
    }
}

