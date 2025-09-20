/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mecanico.application.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.util.ByteArrayDataSource;

@Service
public class EmailService {

    @Value("${app.mail.from}")
    private String from;
    @Value("${app.mail.invoice.content}")
    private String invoiceContent;
    @Value("${app.mail.invoice.kas}")
    private String kasContent;
    @Value("${app.mail.invoice.subject}")
    private String subject;
    @Value("${app.mail.invoice.subject.kas}")
    private String subjectKas;
    @Value("${app.mail.smtp.user}")
    private String user;
    @Value("${app.mail.smtp.pwd}")
    private String pwd;
    @Value("${app.mail.smtp.auth}")
    private String auth;
    @Value("${app.mail.smtp.starttls.enable}")
    private String starttls;
    @Value("${app.mail.smtp.host}")
    private String host;
    @Value("${app.mail.smtp.port}")
    private String port;
    @Value("${app.mail.smtp.ssl.protocols}")
    private String protocol;

    public boolean sendEmail(String to, String content, String attachment) throws Exception {
        try {
            String emailSubject = content.equals("INVOICE") ? subject : subjectKas;
            String text = content.equals("INVOICE") ? invoiceContent : kasContent;
            String fileName = invoiceContent.equals("INVOICE") ? "Factuur" : "Kasboek";
            Properties properties = System.getProperties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.user", user);
            properties.put("mail.smtp.pwd", pwd);
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Authenticator auth = new Authenticator() {
                //override the getPasswordAuthentication method
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pwd);
                }
            };
            Session session = Session.getInstance(properties, auth);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(emailSubject);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(text);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            byte[] attachmentData = attachment.getBytes(StandardCharsets.UTF_8);
            DataSource source = new ByteArrayDataSource(attachmentData, "application/octet-stream");

            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName + ".html");
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return true;
    }
}
