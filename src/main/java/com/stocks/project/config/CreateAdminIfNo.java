package com.stocks.project.config;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.model.Role;
import com.stocks.project.model.StockUser;
import com.stocks.project.model.UserRegistrationDTO;
import com.stocks.project.repository.UserRepository;
import com.stocks.project.security.model.SecurityCredentials;
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
    public Optional<StockUser> createAdminIfThereIsNo() throws EmailOrUsernameIsAlreadyUsedException {
        String username = "admin";

        if (credentialsRepository.findByUserLogin(username).isPresent()) {
            log.info("Admin with username 'admin' has been found");
            return Optional.empty();
        }
        log.info("No default admin has been found in the database");

        String email = "admin@gmail.com";
        String password = "adminroot";

        userRepository.register(
                UserRegistrationDTO.builder()
                        .firstName(username)
                        .email(email)
                        .username(username)
                        .password(password)
                        .build(),
                Role.ADMIN
        );
        log.info("Admin has been created with username: " + username);

        Optional<SecurityCredentials> user = credentialsRepository.findByUserLogin(username);
        if (user.isPresent()) {
            return userRepository.findById(user.get().getId());
        }
        else {
            return Optional.empty();
        }
    }
}
