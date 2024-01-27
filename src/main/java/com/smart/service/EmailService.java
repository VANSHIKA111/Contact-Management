package com.smart.service;

//import java.net.Authenticator;
//import java.net.PasswordAuthentication;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	public boolean sendEmail(String subject, String message, String to) {
		boolean f = false;
		String from = "shinsangyoo.28@gmail.com";

		// variablr for gmail

		String host = "smtp.gmail.com";

		Properties properties = System.getProperties();
		System.out.println("prop :" + properties);

		// host set

		// Corrected properties
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");


		// step 1

		Session session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("shinsangyoo.28@gmail.com", "cecx mppv ijjc xtsz");
			}
		});

		session.setDebug(true);

		// step 2 compose the message

		MimeMessage m = new MimeMessage(session);
		try {

			// from email
			m.setFrom(from);
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			m.setSubject(subject);

			// adding text to message

			m.setText(message);
			
			//m.setContent("message" ,"text/html");
			

			// send

			// step 3 send the message using transport class

			Transport.send(m);
			System.out.println("sent successfull....");
			f = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}
}