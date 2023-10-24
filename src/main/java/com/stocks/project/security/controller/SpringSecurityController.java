package com.stocks.project.security.controller;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.model.Role;
import com.stocks.project.model.User;
import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.security.model.AuthRequest;
import com.stocks.project.security.model.AuthResponse;
import com.stocks.project.security.service.SpringSecurityService;
import com.stocks.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SpringSecurityController {
    private final SpringSecurityService springSecurityService;
    private final UserService userService;

    @Operation(description = "Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401",
                    description = "Incorrect credentials.",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "Successfully authenticated.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            )
    })
    @PostMapping("/authentication")
    public ResponseEntity<AuthResponse> generateToken(@RequestBody AuthRequest authRequest){
        String token = springSecurityService.generateToken(authRequest);
        if (token.isBlank()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(new AuthResponse(token), HttpStatus.OK);
    }

    @Operation(description = "Registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "Email or username is already used",
                    content = @Content),
            @ApiResponse(responseCode = "201",
                    description = "Successfully registered.",
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserSecurityDTO dto) {
        try {
            userService.register(dto, Role.USER);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (EmailOrUsernameIsAlreadyUsedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                    {
                        "error" : "Email or username is already used"
                    }
                    """);
        }
    }
}
