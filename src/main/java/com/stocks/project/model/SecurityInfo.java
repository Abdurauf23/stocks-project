package com.stocks.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "security_info")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SecurityInfo {
    @Id
    @Column(name = "user_id")
    private int userId;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;
}
