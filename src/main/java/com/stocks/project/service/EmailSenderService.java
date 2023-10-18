package com.stocks.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocks.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmailSenderService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    @Value("${spring.mail.username}")
    private String EMAIL_FROM;
    private final String SUBJECT;

    @Autowired
    public EmailSenderService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.SUBJECT = "Daily Stocks Values";
    }

    @Scheduled(cron = "10 39 8 ? * *") // sends emails at 8 am
    public void sendEmail() throws JsonProcessingException {
        log.info("Starting sending emails");

        List<Pair<Integer, String>> usersWithFav = userRepository.getPeopleWithFavStocks();
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleMailMessage message;

        for (Pair<Integer, String> user : usersWithFav) {
            String text = objectMapper.writeValueAsString(userRepository.getAllFavouriteStocks(user.a));
            String email = user.b;
            message = new SimpleMailMessage();

            message.setFrom(EMAIL_FROM);
            message.setTo(email);
            message.setSubject(SUBJECT);
            message.setText(text);
            mailSender.send(message);
        }
        log.info("Finished sending emails");
    }
}
