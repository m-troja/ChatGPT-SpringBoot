package com.michal.openai.functions.impl;

import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.EmailData;
import com.michal.openai.functions.Function;
import com.michal.openai.mail.GmailService;

@Slf4j
public class SendEmailFunction implements Function {

	@Autowired
	private GmailService gmailService;
	@Autowired
	private ObjectMapper objectMapper;
		   
	@Override
	public String execute(String arguments) {
		EmailData emailData = null;
		try {
            emailData = objectMapper.readValue(arguments, EmailData.class);
		} 
		catch (JsonProcessingException e) {
			log.error("Error processing emailData: {}", e.getMessage());
		}

		if (emailData !=null && emailData.emailAddress() == null & emailData.emailAddress() == null) {
			return "Email was not sent. Please, specify full name or email of addressee";
		}
        if (gmailService.sendEmail(emailData)) {
			return "Email was sent successfully";
		} else {
			return  "Email was not sent. Some exception happened, please, try one more time later";
		}
	}
}

