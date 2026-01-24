package org.app.courseapp.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:9989}")
    private String frontendUrl;

    @Override
    public void sendSignUpConfirmationEmail(String email, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Welcome to Course App - Account Created Successfully!");

            String htmlContent = String.format(
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif;'>" +
                            "<div style='background-color: #f4f4f4; padding: 20px;'>" +
                            "<div style='background-color: white; padding: 30px; border-radius: 5px;'>" +
                            "<h2 style='color: #333;'>Welcome to TauBaGo, %s!</h2>" +
                            "<p style='color: #666; line-height: 1.6;'>Your account has been successfully created.</p>" +
                            "<p style='color: #666; line-height: 1.6;'>You can now log in to your account and start managing your health.</p>" +
                            "<p style='color: #666; line-height: 1.6;'>" +
                            "<strong>Account Details:</strong><br/>" +
                            "Email: %s" +
                            "</p>" +
                            "<p style='margin-top: 20px;'>" +
                            "<a href='%s/login' style='background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>" +
                            "Login to Your Account" +
                            "</a>" +
                            "</p>" +
                            "<hr style='margin-top: 30px; border: none; border-top: 1px solid #ddd;'/>" +
                            "<p style='color: #999; font-size: 12px;'>If you did not create this account, please ignore this email.</p>" +
                            "</div>" +
                            "</div>" +
                            "</body>" +
                            "</html>",
                    name, email, frontendUrl
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Sign-up confirmation email sent successfully to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send sign-up confirmation email to: {}", email, e);
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String name, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("TauBaGo – Сброс пароля");

            String resetLink = frontendUrl + "/reset-password";

            String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; margin:0; padding:20px; background:#f8f9fa;">
              <div style="max-width:600px; margin:0 auto; background:white; border-radius:10px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.1);">
                <div style="background:#2196F3; color:white; padding:30px; text-align:center;">
                  <h1 style="margin:0;">TauBaGo</h1>
                </div>
                <div style="padding:40px 30px;">
                  <h2 style="color:#333;">Запрос на сброс пароля</h2>
                  <p style="color:#666; line-height:1.6; font-size:16px;">Привет, %s!</p>
                  <p style="color:#666; line-height:1.6; font-size:16px;">
                    Вы получили это письмо, потому что был сделан запрос на сброс пароля для вашего аккаунта.
                  </p>
                  <div style="text-align:center; margin:40px 0;">
                    <a href="%s" 
                       style="background:#2196F3; color:white; padding:14px 32px; text-decoration:none; border-radius:8px; font-size:16px; font-weight:bold; display:inline-block;">
                      Сбросить пароль
                    </a>
                  </div>
                  <p style="color:#666; line-height:1.6; font-size:16px;">
                    Или скопируйте ссылку и вставьте в браузер:<br>
                    <br>
                    <span style="word-break:break-all; color:#2196F3; font-size:14px;">%s</span>
                  </p>
                  <p style="color:#d32f2f; font-weight:bold; margin-top:30px;">
                    Ссылка действительна только 1 час.
                  </p>
                  <hr style="border:none; border-top:1px solid #eee; margin:30px 0;">
                  <p style="color:#999; font-size:13px;">
                    Если вы не запрашивали сброс пароля — просто проигнорируйте это письмо.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(name, resetLink, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Не удалось отправить письмо", e);
        }
    }
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
