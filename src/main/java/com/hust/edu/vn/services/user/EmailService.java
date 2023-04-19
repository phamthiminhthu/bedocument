package com.hust.edu.vn.services.user;

public interface EmailService {
    public boolean sendSimpleMessage(String to, String subject, String text);
}
