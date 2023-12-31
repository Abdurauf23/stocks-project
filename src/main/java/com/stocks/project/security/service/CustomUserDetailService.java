package com.stocks.project.security.service;

import com.stocks.project.security.model.SecurityCredentials;
import com.stocks.project.security.repository.SecurityCredentialsRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private final SecurityCredentialsRepository securityCredentialsRepository;

    public CustomUserDetailService(SecurityCredentialsRepository securityCredentialsRepository) {
        this.securityCredentialsRepository = securityCredentialsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SecurityCredentials> securityCredentials = securityCredentialsRepository.findByUserLogin(username);
        if (securityCredentials.isEmpty()){
            throw new UsernameNotFoundException(username);
        }
        return User
                .withUsername(securityCredentials.get().getLogin())
                .password(securityCredentials.get().getPassword())
                .roles(securityCredentials.get().getRole().name())
                .build();
    }
}
