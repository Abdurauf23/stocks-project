package com.stocks.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "second_name")
    private String secondName;

    @Column(name = "birthday")
    private Date birthday;

    public User(String firstName, String secondName, Date birthday) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.birthday = birthday;
    }
}
