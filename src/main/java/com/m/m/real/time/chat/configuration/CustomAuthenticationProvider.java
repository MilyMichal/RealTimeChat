package com.m.m.real.time.chat.configuration;

import com.m.m.real.time.chat.errors.AccountAlreadyLoggedInException;
import com.m.m.real.time.chat.services.AppUserDetailService;
import com.m.m.real.time.chat.services.OnlineUserService;
import com.m.m.real.time.chat.services.UserStorageService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {


    private final OnlineUserService onlineUserService;
    private final AppUserDetailService appUserDetailService;
    private final PasswordEncoder passwordEncoder;
    private final UserStorageService userStorageService;

    public CustomAuthenticationProvider(OnlineUserService onlineUserService, AppUserDetailService appUserDetailService, PasswordEncoder passwordEncoder, UserStorageService userStorageService) {
        this.onlineUserService = onlineUserService;
        this.appUserDetailService = appUserDetailService;


        this.passwordEncoder = passwordEncoder;
        this.userStorageService = userStorageService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails userDetails = appUserDetailService.loadUserByUsername(authentication.getName());

        if (!userDetails.isAccountNonLocked()) {

            throw new LockedException(String.format(
                    "Your account is BANNED! <br> BAN will expire on: %s", userStorageService.getUser(authentication.getName())
                            .getBanExpiration()
                            .format(DateTimeFormatter.ofPattern("dd-M-yyyy H:mm:ss"))));
        }

        if (onlineUserService.findOnlineUser(userDetails.getUsername()).isPresent()) {
            throw new AccountAlreadyLoggedInException("User \"" + authentication.getName() + "\" is already online");
        }

        if (!passwordEncoder.matches(authentication.getCredentials().toString(),userDetails.getPassword())) {
            throw new BadCredentialsException("Incorrect password! Try again.");
        }


        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }


}
