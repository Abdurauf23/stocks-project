package com.stocks.project.controller;

import com.stocks.project.model.SecurityInfo;
import com.stocks.project.service.SecurityInfoService;
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
    private final SecurityInfoService securityInfoService;

    @Autowired
    public SecurityController(SecurityInfoService securityInfoService) {
        this.securityInfoService = securityInfoService;
    }

    @GetMapping
    public List<SecurityInfo> getAll() {
        return securityInfoService.findAll();
    }

    @GetMapping("/{userId}")
    public SecurityInfo getById(@PathVariable int userId) {
        return securityInfoService.findById(userId);
    }

    @PostMapping("/{userId}")
    public SecurityInfo create(@RequestBody SecurityInfo securityInfo, @PathVariable int userId) {
        return securityInfoService.create(securityInfo, userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(HttpServletResponse response, @PathVariable int userId) throws IOException {
        securityInfoService.delete(userId);
        response.sendRedirect("/security-info");
    }

    @PutMapping("/{userId}")
    public SecurityInfo update(@RequestBody SecurityInfo updatedInfo, @PathVariable int userId) {
        return securityInfoService.update(updatedInfo, userId);
    }
}
