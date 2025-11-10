package com.michal.openai.mail.impl;

import com.michal.openai.entity.EmailData;
import com.michal.openai.mail.GmailService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
@Slf4j
@Service
public class DefaultGmailService implements GmailService {

	@Value("${mail.sender.email}")
	private String senderEmail;
	@Value("${mail.sender.email.password}")
	private String senderPassword;
	@Value("${mail.receiver.test.email}")
	private String receiverTestEmail;
	
	@Override
	public boolean sendEmail(String addresseeEmail, String subject, String content) {
		// SMTP server configuration
		String smtpHost = "smtp.gmail.com";
        log.info("smtpHost {}", smtpHost);

		int smtpPort = 465;
        log.info("smtpPort {}", smtpPort);

		try {
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.socketFactory.class", "jakarta.net.ssl.SSLSocketFactory");
			props.put(Optional.of("mail.smtp.socketFactory.port"), smtpPort);
			props.put("mail.smtp.host", smtpHost);
			props.put(Optional.of("mail.smtp.port"), smtpPort);

			// Create a Session object with authentication
			Session session = Session.getInstance(props, new Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(senderEmail, senderPassword);
	            }
	        });

			// Create the email message
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(senderEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(addresseeEmail));
			message.setSubject(subject);
            log.info("message {}", message.toString());
			
			// Create multipart content for the email
			Multipart multipart = new MimeMultipart();

			// Create the text part of the email
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(content);
			multipart.addBodyPart(textPart);
            log.info("multipart {}", multipart.toString());

			// Set the multipart content as the email's content
			message.setContent(multipart);

			// Send the email
			Transport.send(message);

			log.info("Email was sent successfully.");
			return true;
		} catch (MessagingException e) {
			log.error("Error sending email: ", e);
			return false;
		}

	}

	@Override
	public boolean sendEmail(EmailData emailData) {
        log.info("emailData {}", emailData.toString());

		return sendEmail(emailData.emailAddress(), emailData.subject(), emailData.content());
	}

	/* TODO: implement integration with email addresses data source. 
	 */
	@Override
	public String extractEmailByFullName(String addresseeName) {
		Map<String, String> fullNameToEmailMap = new HashMap<>();
		fullNameToEmailMap.put("John Doe", receiverTestEmail);
        log.debug("extractEmailByFullName fullNameToEmailMap : {}", fullNameToEmailMap);
        log.debug("extractEmailByFullName emailAddress : {}", addresseeName);

		return fullNameToEmailMap.get(addresseeName);
	}

}