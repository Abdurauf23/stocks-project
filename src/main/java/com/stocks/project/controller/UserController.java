package com.stocks.project.controller;

import com.stocks.project.model.User;
import com.stocks.project.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public User getById(@PathVariable int userId) {
        return userService.findById(userId);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(HttpServletResponse response, @PathVariable int userId) throws IOException {
        userService.deleteUser(userId);
        response.sendRedirect("/users");
    }

    @PutMapping("/{userId}")
    public User updateUser(@RequestBody User updatedUser,
                           @PathVariable int userId) {
        return userService.updateUser(updatedUser, userId);
    }
}
