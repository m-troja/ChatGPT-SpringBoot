package com.michal.openai.functions.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.EmailData;
import com.michal.openai.functions.Function;
import com.michal.openai.mail.GmailService;


public class SendEmailFunction implements Function {

	@Autowired
	private GmailService gmailService;
	@Autowired
	private ObjectMapper objectMapper;
		   
	@Override
	public CompletableFuture<String> execute(String arguments) {
		EmailData emailData = new EmailData();
		try {
			emailData = objectMapper.readValue(arguments, EmailData.class);
		} 
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		 
		if (emailData.getAddresseeEmail() == null & emailData.getAddresseeName() == null) {
			return CompletableFuture.completedFuture("Email was not sent. Please, specify full name or email of addressee");
		}
		if (emailData.getAddresseeEmail() == null & emailData.getAddresseeName() != null) {
			emailData.setAddresseeEmail(gmailService.extractEmailByFullName(emailData.getAddresseeName()));
			if (emailData.getAddresseeEmail() == null) {
				return CompletableFuture.completedFuture("Email was not sent. Please, specify email of addressee");
			}
		}
		
		if (gmailService.sendEmail(emailData)) {
			return CompletableFuture.completedFuture("Email was sent successfully");
		} else {
			return  CompletableFuture.completedFuture("Email was not sent. Some exception is happened, please, try one more time later");
		}
	}
}

