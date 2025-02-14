package com.emosation.emosation.EmailServiceTest;


import com.emosation.emosation.sevices.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;


    @Test
    public void testSendEmail() throws Exception {

        String to = "bydongeun@gmail.com";

        String subject = "사랑해";

        String content = "테스트메일입니다";


        emailService.sendMail(to,subject,content);




    }



}
