package com.michal.openai.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.michal.openai.gpt.impl.DefaultGptService;

@RequestMapping( value = {"/chatgpt", "/"} )
@Controller
public class ChatGptController {
	
	@Autowired
	DefaultGptService gptService;
	
	@GetMapping
	public String doGet(Model model) {
	
		return "homepage";
	}
	
	@PostMapping
	public String doPost(@RequestParam String query, Model model) {
		
		String response = gptService.getAnswerToSingleQuery(query);
		model.addAttribute("response", response);
		
		return "homepage";
	}

	  public void addCorsMappings(CorsRegistry registry) {
	        registry.addMapping("/v1/**")
	                .allowedOrigins("*")  
	                .allowedMethods("GET")  
	                .allowedHeaders("*")
	              //  .allowCredentials(true)
	                ;
	        registry.addMapping("/chatgpt")
	        .allowedOrigins("*")  
	        .allowedMethods("GET", "POST")  
	        .allowedHeaders("*")
	      //  .allowCredentials(true)
	        ;
	    }
}
