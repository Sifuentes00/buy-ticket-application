package com.matvey.cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTicketConfirmation(String toEmail, String movieTitle, String dateTime, String seats) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@cinema-app.ru");
        message.setTo(toEmail);
        message.setSubject("Ваш билет в кино!");
        message.setText("Поздравляем с покупкой!\n\n" +
                "Фильм: " + movieTitle + "\n" +
                "Сеанс: " + dateTime + "\n" +
                "Места: " + seats + "\n\n" +
                "Приятного просмотра!");

        mailSender.send(message);
    }
}