package com.stocks.project.security.controller;

import com.stocks.project.model.Role;
import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.security.model.AuthRequest;
import com.stocks.project.security.model.AuthResponse;
import com.stocks.project.security.service.SpringSecurityService;
import com.stocks.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringSecurityController {
    private final SpringSecurityService springSecurityService;
    private final UserService userService;


    @Autowired
    public SpringSecurityController(SpringSecurityService springSecurityService, UserService userService) {
        this.springSecurityService = springSecurityService;
        this.userService = userService;
    }

    @PostMapping("/authentication")
    public ResponseEntity<AuthResponse> generateToken(@RequestBody AuthRequest authRequest){
        String token = springSecurityService.generateToken(authRequest);
        if (token.isBlank()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserSecurityDTO dto) {
        try {
            userService.register(dto, Role.USER);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            
                            """);
        }
    }
}
