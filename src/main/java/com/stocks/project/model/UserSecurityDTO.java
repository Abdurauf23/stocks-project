package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;

@AllArgsConstructor
@Data
public class UserSecurityDTO {
    private String firstName;
    private String secondName;
    private String email;
    private String username;
    private String password;
    private Date birthday;
}
