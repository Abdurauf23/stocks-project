package com.stocks.project.controller;

import com.stocks.project.exception.EmailOrUsernameIsAlreadyUsedException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.ErrorModel;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.model.StockUser;
import com.stocks.project.service.SecurityInfoService;
import com.stocks.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/security-info")
@SecurityRequirement(name = "Bearer Authentication")
public class SecurityInfoController {
    private final SecurityInfoService securityInfoService;
    private final UserService userService;

    public SecurityInfoController(SecurityInfoService securityInfoService,
                                  UserService userService) {
        this.securityInfoService = securityInfoService;
        this.userService = userService;
    }

    @Operation(description = "List of all users security info in DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "For user: User cannot see all security info.",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "For admin: List of all users in DB (JSON array).",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockUser.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<SecurityInfo>> getAll() {
        return new ResponseEntity<>(securityInfoService.findAll(), HttpStatus.OK);
    }

    @Operation(description = "Get security info for particular user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404",
                    description = "For administrator: No such user in DB.",
                    content = @Content(mediaType = "application.json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "403",
                    description = "For user: If user wants to access another users security info.",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "User is found in DB.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SecurityInfo.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> getById(@PathVariable int userId, Principal principal) {
        Optional<SecurityInfo> securityInfo = securityInfoService.findById(userId);

        String login = principal.getName();
        if (userService.isAdmin(login) || userService.isSame(login, userId)) {
            if (securityInfo.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                        {
                            "error" : "No such user"\s
                        }
                        """);
            }
            return new ResponseEntity<>(securityInfo.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Operation(description = "Creating a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "Not enough data for registration OR Email or username is already used",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "404",
                    description = "No User with this ID.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "403",
                    description = "For user: If user wants to create.",
                    content = @Content),
            @ApiResponse(responseCode = "201",
                    description = "Security info is successfully created in DB.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SecurityInfo.class)))
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody SecurityInfo newSecurityInfo) {
        try {
            int userId = newSecurityInfo.getUserId();
            Optional<SecurityInfo> securityInfo = securityInfoService.create(newSecurityInfo, userId);
            if (securityInfo.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                        {
                            "error" : "Bad request"
                        }
                        """);
            }
            return new ResponseEntity<>(securityInfo.get(), HttpStatus.CREATED);
        } catch (NotEnoughDataException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                    {
                        "error":"Not enough data"
                    }
                    """);
        } catch (NoSuchUserException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                    {
                        "error" : "No such user"
                    }
                    """);
        } catch (EmailOrUsernameIsAlreadyUsedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                    {
                        "error" : "Email or username is already used"
                    }
                    """);
        }
    }

    @Operation(description = "Deleting a Security info for User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404",
                    description = "For admin: No such user.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "403",
                    description = "For user: If user wants to delete another users security info.",
                    content = @Content),
            @ApiResponse(responseCode = "204",
                    description = "Security info is successfully deleted",
                    content = @Content)
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> delete(@PathVariable int userId) {
        try {
            securityInfoService.delete(userId);
        } catch (NoSuchUserException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                    {
                        "error" : "No such user"
                    }
                    """);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @Operation(description = "Update security info for user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404",
                    description = "For administrator: No such user.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "400",
                    description = "Email or username is already used",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorModel.class))),
            @ApiResponse(responseCode = "403",
                    description = "For user: If user wants to update another users security info.",
                    content = @Content),
            @ApiResponse(responseCode = "200",
                    description = "Users security info is successfully changed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SecurityInfo.class)))
    })
    @PutMapping
    public ResponseEntity<?> update(@RequestBody SecurityInfo updatedInfo,
                                    Principal principal) {
        String login = principal.getName();
        int userId = updatedInfo.getUserId();
        if (userService.isAdmin(login) || userService.isSame(login, userId)) {
            try {
                return new ResponseEntity<>(securityInfoService.update(updatedInfo, userId), HttpStatus.OK);
            } catch (NoSuchUserException e) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                    {
                        "error" : "No such user"
                    }
                    """);
            } catch (EmailOrUsernameIsAlreadyUsedException e) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                    {
                        "error" : "Email or username is already used"
                    }
                    """);
            }
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
