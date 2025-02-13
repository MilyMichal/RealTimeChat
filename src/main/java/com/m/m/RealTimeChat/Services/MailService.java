package com.m.m.RealTimeChat.Services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MailService {

    @Value("${app.api.sendgridapikey}")
    private String APIKEY;




    public void sendEmail(String requestedEmail, String link) throws IOException {
        Email from = new Email("azrael.taleri@gmail.com");
        String subject = "Reset password request";
        Email to = new Email(requestedEmail);
        Content content = new Content("text/plain", "This is your link for reset your password: " + link);
        Mail mail = new Mail(from, subject,to,content);

        SendGrid sg = new SendGrid(APIKEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (IOException e) {
            throw e;
        }
    }
}

