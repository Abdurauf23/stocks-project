package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SecurityInfo {
    private int userId;
    private String username;
    private String password;
    private String email;
    private Role role;
}
