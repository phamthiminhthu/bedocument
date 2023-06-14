package com.hust.edu.vn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserDto implements Serializable {
    private Long id;
    private String email;
    private String username;
    private String fullname;
    private Byte gender;
    private String phone;
    private LocalDate birthday;
    private String address;
    private String image;
    private String introduce;
    private Date createdAt;
    private Date updatedAt;

}