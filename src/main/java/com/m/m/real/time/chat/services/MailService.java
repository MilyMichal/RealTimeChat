package com.m.m.real.time.chat.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;

import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MailService {

    private static final Logger logger = LogManager.getLogger(MailService.class);

    @Value("${app.api.sendgridApiKey}")
    private String apiKey;
    @Value("${app.api.sendgridTemplateId}")
    private String templateId;

    public void sendEmail(String requestedEmail, String link) throws IOException {


        Email from = new Email("azrael.taleri@gmail.com","Real-time chat app support");
        String subject = "Reset password request";
        Email to = new Email(requestedEmail);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("url", link);
        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("/mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            logger.log(Level.INFO, String.format("Request status code : %s",  response.getStatusCode()));
            logger.log(Level.INFO, String.format("Header : %s", response.getHeaders()));
            logger.log(Level.INFO, String.format("Body: %s" ,response.getBody()));
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
    }
}

