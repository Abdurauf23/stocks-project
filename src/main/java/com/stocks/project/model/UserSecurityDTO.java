package com.stocks.project.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserSecurityDTO {
    private String firstName;
    private String secondName;
    private String email;
    private String username;
    private String password;
    private Date birthday;
}
