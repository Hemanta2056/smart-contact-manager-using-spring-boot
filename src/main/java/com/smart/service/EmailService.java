package com.smart.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    public boolean sendEmail(String subject, String messageText, String to) {
        boolean f = false;

        String from = "demoa2393@gmail.com";
        String host = "smtp.gmail.com";

        // Set up the properties
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("demoa2393@gmail.com", "nfwx hsva svqf pbfq");
            }
        });

        session.setDebug(true);
        
        MimeMessage message = new MimeMessage(session);

        try {
            // Create a MimeMessage using the session
          

            // Set the sender address
            message.setFrom(new InternetAddress(from));

            // Set the recipient address
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set the email subject and content
            message.setSubject(subject);
            message.setText(messageText);
            
            
            //message.setContent(message, "text/html");


            // Send the message using Transport class
            Transport.send(message);
            System.out.println("sent success");
            f = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return f;
    }
}
