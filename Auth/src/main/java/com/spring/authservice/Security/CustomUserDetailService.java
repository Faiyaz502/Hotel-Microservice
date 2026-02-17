package com.spring.authservice.Security;



import com.spring.authservice.user.Entity.User;
import com.spring.authservice.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        Optional<User> userOpt;

        //  Auto-detect if login is email or username
        if (username.contains("@")) {
            userOpt = userRepository.findByEmail(username);
        } else {
            userOpt = userRepository.findByUsername(username);
        }

        return userOpt.orElseThrow(() ->
                new UsernameNotFoundException("No user found for: " + username)
        );
    }
}
