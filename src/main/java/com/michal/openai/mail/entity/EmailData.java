package com.michal.openai.mail.entity;

public record EmailData(String emailAddress, String name, String subject, String content) {}