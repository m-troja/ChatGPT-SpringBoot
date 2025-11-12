package com.michal.openai.mail;

import com.michal.openai.mail.entity.EmailData;

public interface GmailService {

	boolean sendEmail(String addresseeEmail, String subject, String content);


	String extractEmailByFullName(String addresseeName);


	boolean sendEmail(EmailData emailData);




	
}
