package org.app.courseapp.service;

public interface EmailService {
    void sendSignUpConfirmationEmail(String email, String name);
    void sendEmail(String to, String subject, String body);
    void sendPasswordResetEmail(String email, String name, String resetToken);

}
