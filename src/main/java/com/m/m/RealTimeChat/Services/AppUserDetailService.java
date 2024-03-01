package com.m.m.RealTimeChat.Services;

import com.m.m.RealTimeChat.Models.SecurityUser;
import com.m.m.RealTimeChat.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       return userRepository.findUserByUserName(username)
               .map(SecurityUser::new).orElseThrow(()-> new UsernameNotFoundException("User doesn't exist"));
    }
}
