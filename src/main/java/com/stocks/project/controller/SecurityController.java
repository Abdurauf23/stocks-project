package com.stocks.project.controller;

import com.stocks.project.model.SecurityInfo;
import com.stocks.project.service.SecurityService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/security-info")
public class SecurityController {
    private final SecurityService securityService;

    @Autowired
    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @GetMapping
    public List<SecurityInfo> getAll() {
        return securityService.findAll();
    }

    @GetMapping("/{userId}")
    public SecurityInfo getById(@PathVariable int userId) {
        return securityService.findById(userId);
    }

    @PostMapping("/{userId}")
    public SecurityInfo create(@RequestBody SecurityInfo securityInfo, @PathVariable int userId) {
        return securityService.create(securityInfo, userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(HttpServletResponse response, @PathVariable int userId) throws IOException {
        securityService.delete(userId);
        response.sendRedirect("/security-info");
    }

    @PutMapping("/{userId}")
    public SecurityInfo update(@RequestBody SecurityInfo updatedInfo, @PathVariable int userId) {
        return securityService.update(updatedInfo, userId);
    }
}
