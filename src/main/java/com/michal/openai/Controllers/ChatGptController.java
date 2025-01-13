package com.michal.openai.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping( value = {"/chatgpt", "/"} )
public class ChatGptController {
	
	@GetMapping
	public String doGet(Model model) {
	
		return "homepage";
	}
	
	@PostMapping
	public String doPost(Model model) {
	
		return "homepage";
	}
}
