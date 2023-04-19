package com.hust.edu.vn.model;

import com.hust.edu.vn.entity.Document;
import com.hust.edu.vn.entity.Follow;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePublicModel {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Email
    private String username;
    private String fullname;
    private Byte gender;
    private String image;
    private String introduce;
    private LocalDate birthday;
    private String address;
    private Date createdAt;
    private ArrayList<Document> documents;
    private ArrayList<Follow> followers;
    private ArrayList<Follow> following;

}
