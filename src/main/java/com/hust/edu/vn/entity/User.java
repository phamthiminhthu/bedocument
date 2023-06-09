package com.hust.edu.vn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "gender")
    private Byte gender;

    @Column(name = "phone", length = 11)
    private String phone;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name="rootPath")
    private String rootPath;

    @Lob
    @Column(name = "address")
    private String address;

    @Lob
    @Column(name = "image")
    private String image;

    @Column(name = "introduce")
    private String introduce;

    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date();

}