package com.hust.edu.vn.repository;

import com.hust.edu.vn.entity.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Long> {
    ResetPasswordToken findByToken(String token);

    void deleteById(ResetPasswordToken resetPasswordToken);
}