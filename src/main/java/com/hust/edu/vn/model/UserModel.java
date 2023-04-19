package com.hust.edu.vn.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String username;

    @NotBlank
    @Size(min=8)
    private String password;

    @NotBlank
    @Size(min=8)
    private String confirmPassword;

    @AssertTrue(message="Password and Confirm Password must be the same!")
    public boolean isConfirmPassword(){
        return password.equals(confirmPassword);
    }


}
