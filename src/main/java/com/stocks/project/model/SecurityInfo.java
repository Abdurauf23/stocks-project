package com.stocks.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    public SecurityInfo(int userId, String username, String password, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
