package com.stocks.project.controller;

import com.stocks.project.exception.NoSuchUserException;
import com.stocks.project.exception.NotEnoughDataException;
import com.stocks.project.model.SecurityInfo;
import com.stocks.project.service.SecurityInfoService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/security-info")
@RequiredArgsConstructor
public class SecurityController {
    private final SecurityInfoService securityInfoService;

    @GetMapping
    public ResponseEntity<List<SecurityInfo>> getAll() {
        return new ResponseEntity<>(securityInfoService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getById(@PathVariable int userId) {
        Optional<SecurityInfo> securityInfo = securityInfoService.findById(userId);
        if (securityInfo.isPresent()) {
            return new ResponseEntity<>(securityInfo.get(), HttpStatus.OK);
        }
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                {
                    "error" : "No such user"\s
                }
                """);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> create(@RequestBody SecurityInfo newSecurityInfo, @PathVariable int userId) {
        try {
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
                        "error" : "No such user"\s
                    }
                    """);
        }
    }

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
                        "error" : "No such user"\s
                    }
                    """);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> update(@RequestBody SecurityInfo updatedInfo, @PathVariable int userId) {
        try {
            return new ResponseEntity<>(securityInfoService.update(updatedInfo, userId), HttpStatus.OK);
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
}
