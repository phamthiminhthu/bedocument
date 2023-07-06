package com.hust.edu.vn.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordModel {

    @NotBlank
    private String oldPassword;
    @NotBlank
    @Size(min=8)
    private String newPassword;
    @NotBlank
    @Size(min=8)
    private String confirmPassword;
}
