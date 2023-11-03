package com.stocks.project.controller;

import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.ErrorModel;
import com.stocks.project.model.StockUser;
import com.stocks.project.security.model.SecurityCredentials;
import com.stocks.project.security.repository.SecurityCredentialsRepository;
import com.stocks.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final UserService userService;
    private final SecurityCredentialsRepository credentialsRepository;

    @Autowired
    public UserController(UserService userService, SecurityCredentialsRepository credentialsRepository) {
        this.userService = userService;
        this.credentialsRepository = credentialsRepository;
    }

    @Operation(description = "List of all users in DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "For user: User cannot access this function.",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "For admin: List of all users in DB (JSON array).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockUser.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<StockUser>> getAll() {
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @Operation(description = "Get user with particular ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404",
                    description = "For admin: No such stockUser in DB.",
                    content = @Content(mediaType = "application.json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "403",
                    description = "For user: If user wants to access another user.",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "If user is found in DB.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockUser.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getById(@PathVariable int userId, Principal principal) {
        Optional<StockUser> stockUser = userService.findById(userId);

        String login = principal.getName();
        if (userService.isAdmin(login) || userService.isSame(login, userId)){
            if (stockUser.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                        {
                            "error" : "No such user"
                        }
                        """);
            }
            return new ResponseEntity<>(stockUser.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Operation(description = "Creating a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "For admin: User should have first name.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "403",
                    description = "For user: If user wants to create.",
                    content = @Content),
            @ApiResponse(responseCode = "201",
                    description = "For admin: User is successfully created in DB ",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockUser.class)))
    })
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody StockUser newStockUser) {
        try {
            Optional<StockUser> stockUser = userService.createUser(newStockUser);
            if (stockUser.isPresent()) {
                return new ResponseEntity<>(stockUser.get(), HttpStatus.CREATED);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NoFirstNameException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                    {
                        "error" : "'firstName' is required to create a user." \s
                    }
                    """);
        }
    }

    @Operation(description = "Deleting a User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404",
                    description = "For admin: No such user.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "403",
                    description = "For user: If user wants to delete another user.",
                    content = @Content),
            @ApiResponse(responseCode = "204",
                    description = "User is successfully deleted",
                    content = @Content)
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId, Principal principal) {
        String login = principal.getName();
        if (userService.isSame(login, userId) || userService.isAdmin(login)) {
            try {
                userService.delete(userId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (NoSuchUserException e) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                    "error" : "No such user"\s
                                }
                                """);
            }
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Operation(description = "Update a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404",
                    description = "For admin: No such user.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "403",
                    description = "For user: If user wants to update another user.",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "User is successfully changed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockUser.class)))
    })
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody StockUser updatedStockUser,
                                        Principal principal) {
        String login = principal.getName();
        int userId = updatedStockUser.getUserId();
        if (userService.isAdmin(login) || userService.isSame(login, userId)) {
            try {
                return new ResponseEntity<>(userService.updateUser(updatedStockUser, userId), HttpStatus.OK);
            } catch (NoSuchUserException e) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                        {
                            "error" : "No such user"\s
                        }
                        """);
            }
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Operation(description = "Get own account information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Just self user information",
                    content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = StockUser.class))),
            @ApiResponse(responseCode = "500",
                    description = "Something went wrong",
                    content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorModel.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<?> getSelf(Principal principal) {
        Optional<SecurityCredentials> byUserLogin = credentialsRepository.findByUserLogin(principal.getName());
        if (byUserLogin.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                          {
                            "error": "Something went wrong. Try authenticating one more time."
                          }
                          """);
        }
        return new ResponseEntity<>(userService.findById(byUserLogin.get().getId()),HttpStatus.OK);
    }
}
