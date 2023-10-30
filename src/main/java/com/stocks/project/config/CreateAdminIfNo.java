package com.stocks.project.config;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.model.Role;
import com.stocks.project.model.User;
import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.repository.UserRepository;
import com.stocks.project.security.repository.SecurityCredentialsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@Slf4j
public class CreateAdminIfNo {
    public final UserRepository userRepository;
    public final SecurityCredentialsRepository credentialsRepository;

    @Autowired
    public CreateAdminIfNo(UserRepository userRepository, SecurityCredentialsRepository credentialsRepository) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
    }

    @Bean
    public Optional<User> createAdminIfThereIsNo() throws EmailOrUsernameIsAlreadyUsedException {
        String username = "admin";

        if (credentialsRepository.findByUserLogin(username).isPresent()) {
            return Optional.empty();
        }
        log.info("No default admin has been found in the database");

        String email = "admin@gmail.com";
        String password = "adminroot";

        userRepository.register(
                UserSecurityDTO.builder()
                        .firstName(username)
                        .email(email)
                        .username(username)
                        .password(password)
                        .build(),
                Role.ADMIN
        );
        log.info("Admin has been created with username: '" + username + "' and password: '" + password + "'");
        int id = credentialsRepository.findByUserLogin(username).get().getId();
        return userRepository.findById(id);
    }
}
