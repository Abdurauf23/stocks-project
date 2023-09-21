package com.stocks.project.controller;

import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.repository.SecurityRepository;
import com.stocks.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
public class RegisterController {
    private final UserRepository userRepository;
    private final SecurityRepository securityRepository;

    @Autowired
    public RegisterController(UserRepository userRepository, SecurityRepository securityRepository) {
        this.userRepository = userRepository;
        this.securityRepository = securityRepository;
    }

    @PostMapping
    public void register(@RequestBody UserSecurityDTO dto) {

    }
}
