package com.hust.edu.vn.services.impl.user;

import com.hust.edu.vn.services.user.EmailService;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    @Override
    public boolean sendSimpleMessage(String to, String subject, String text) {
        Dotenv dotenv = Dotenv.configure().load();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(dotenv.get("EMAIL_USERNAME"));
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        return true;
    }
}
