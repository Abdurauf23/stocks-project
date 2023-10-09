package com.stocks.project.security.service;

import com.stocks.project.security.model.AuthRequest;
import com.stocks.project.security.model.SecurityCredentials;
import com.stocks.project.security.repository.SecurityCredentialsRepository;
import com.stocks.project.security.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpringSecurityService {
    private final SecurityCredentialsRepository securityCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public SpringSecurityService(SecurityCredentialsRepository securityCredentialsRepository,
                                 PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.securityCredentialsRepository = securityCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public String generateToken(AuthRequest authRequest) {
        Optional<SecurityCredentials> credentials = securityCredentialsRepository.findByUserLogin(authRequest.getLogin());
        if (credentials.isPresent() && passwordEncoder.matches(authRequest.getPassword(), credentials.get().getPassword())) {
            return jwtUtils.generateJwtToken(authRequest.getLogin());
        }
        return "";
    }
}
