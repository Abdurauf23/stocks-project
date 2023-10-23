package com.stocks.project.security.model;

import com.stocks.project.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecurityCredentials {
    private int id;
    private String login;
    private String password;
    private String email;
    private Role role;
}
