package com.michal.openai.controllers;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/v1/admin")
@RestController
@NoArgsConstructor
public class AdminController {

	@PersistenceContext
	private EntityManager entityManager;
	
	/* Endpoint to trigger truncating all tables in DB */
	
	@GetMapping(value = "/clear-database")
	@Transactional
	public String clearDatabase()
	{
        log.info("GET /admin/clear-database");
        deleteAllData();

		return "Cleared database";
	}
	
	@Transactional
	public void deleteAllData() {
	    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
	    entityManager.createNativeQuery("TRUNCATE TABLE gpt_request").executeUpdate();

	    entityManager.createNativeQuery("TRUNCATE TABLE gpt_response").executeUpdate();
	    entityManager.createNativeQuery("TRUNCATE TABLE slack_user").executeUpdate();
	    // ... do this for each table
	    entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
	}
	
}
