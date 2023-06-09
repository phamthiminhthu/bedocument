package com.hust.edu.vn.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
   @NotBlank
   @Email
   private String email;
   @NotBlank
   private String username;
   private MultipartFile avatar;
   private String phone;
   private String address;
   private LocalDate birthday;
   private String fullname;
   private Byte gender;
   private String introduce;
}
