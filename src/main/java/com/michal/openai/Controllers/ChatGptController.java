package com.michal.openai.Controllers;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.michal.openai.gpt.impl.DefaultGptService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping( value = {"/chatgpt", "/"} )
@Controller
public class ChatGptController {
	
	@Autowired
	DefaultGptService gptService;
	
	@GetMapping
	public String doGet(Model model) {
		log.info("GET /chatgpt\", \"/\" " );
		return "homepage";
	}
	
	@PostMapping
	public String doPost(@RequestParam String query, Model model) {
		
		CompletableFuture<String> response = gptService.getAnswerToSingleQuery(CompletableFuture.completedFuture(query));
		model.addAttribute("response", response);
		log.info("POST /chatgpt\", \"/\" " + query );
		return "homepage";
	}
}
