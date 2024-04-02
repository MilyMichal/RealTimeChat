package com.m.m.RealTimeChat.Configuration;

import com.m.m.RealTimeChat.Errors.AccountAlreadyLoggedInException;
import com.m.m.RealTimeChat.Services.AppUserDetailService;
import com.m.m.RealTimeChat.Services.OnlineUserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {


    private final OnlineUserService onlineUserService;
    private final AppUserDetailService appUserDetailService;


    public CustomAuthenticationProvider(OnlineUserService onlineUserService, AppUserDetailService appUserDetailService) {
        this.onlineUserService = onlineUserService;
        this.appUserDetailService = appUserDetailService;
    }

    @Override

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails userDetails = appUserDetailService.loadUserByUsername(authentication.getName());

        /*System.out.println("AUTH DEBUG IS USE ONLINE: " + onlineUserService.findOnlineUser(userDetails.getUsername()).isPresent());*/
        if (onlineUserService.findOnlineUser(userDetails.getUsername()).isPresent()) {
            System.out.println("AUTH DEBUG: AUTHENTICATION FAILED");
            throw new AccountAlreadyLoggedInException("User \"" + authentication.getName() + "\" is already online");
        }

        if (!userDetails.getUsername().equals(authentication.getName())) {
            throw new BadCredentialsException("Username not found");
        }
        if (!userDetails.getPassword().equals(authentication.getCredentials().toString())) {
            throw new BadCredentialsException("Incorrect password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }


}
