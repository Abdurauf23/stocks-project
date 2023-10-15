package com.stocks.project.controller;

import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.User;
import com.stocks.project.service.UserService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAll(Principal principal) {
        if (!userService.isAdmin(principal.getName())){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getById(@PathVariable int userId, Principal principal) {
        Optional<User> user = userService.findById(userId);

        String login = principal.getName();
        if (userService.isAdmin(login)){
            if (user.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                        {
                            "error" : "No such user"\s
                        }
                        """);
            }
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            if (userService.isSame(login, userId)) {
                return new ResponseEntity<>(user.get(), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User newUser) {
        try {
            Optional<User> user = userService.createUser(newUser);
            return new ResponseEntity<>(user.get(), HttpStatus.CREATED);
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

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        try {
            userService.delete(userId);
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
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@RequestBody User updatedUser,
                                        @PathVariable int userId,
                                        Principal principal) {
        String login = principal.getName();
        if (userService.isAdmin(login)) {
            try {
                return new ResponseEntity<>(userService.updateUser(updatedUser, userId), HttpStatus.OK);
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
        } else {
            try {
                if (userService.isSame(login, userId)) {
                    return new ResponseEntity<>(
                            userService.updateUser(updatedUser, userId), HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchUserException e) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }
    }
}
