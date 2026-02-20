package com.michal.openai.controllers;


import com.michal.openai.gpt.GptService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/v1/admin")
@RestController
@RequiredArgsConstructor
public class AdminController {

	private final GptService gptService;

    /* Endpoint to trigger truncating all tables in DB */
	
	@DeleteMapping(value = "clear-database")
	@Transactional
	public String clearDatabase()
	{
        log.info("GET /admin/clear-database");
        gptService.clearDatabase();

		return "Cleared database";
	}


}