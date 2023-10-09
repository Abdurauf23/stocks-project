package com.stocks.project.controller;

import com.stocks.project.exception.NoFirstNameException;
import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.model.User;
import com.stocks.project.model.UserSecurityDTO;
import com.stocks.project.service.UserService;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getById(@PathVariable int userId) {
        Optional<User> user = userService.findById(userId);
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
        return ResponseEntity.of(user);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User newUser) {
        Optional<User> user;
        try {
            user = userService.createUser(newUser);
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
            userService.deleteUser(userId);
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
    public ResponseEntity<?> updateUser(@RequestBody User updatedUser, @PathVariable int userId) {
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
    }

    @GetMapping("/{userId}/fav-stocks")
    public ResponseEntity<?> getFavouriteStocks(@PathVariable int userId) {
        return new ResponseEntity<>(userService.getAllFavouriteStocks(userId), HttpStatus.OK);
    }
}
