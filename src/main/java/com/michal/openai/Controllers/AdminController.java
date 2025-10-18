package com.michal.openai.Controllers;


import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping( value = {"/admin" })
@RestController
@NoArgsConstructor
public class AdminController {

	@PersistenceContext
	private EntityManager entityManager;
	
	
	/* Endpoint to trigger truncating all tables in DB */
	
	@GetMapping(value = "/delete-all")
	@Transactional
	public String clearDatabase(@RequestParam String cmd) 
	{
        log.info("GET /admin -> delete all data, cmd == {}", cmd);

		if ( cmd.equals("ok"))
		{
			deleteAllData();
			return "All data deleted";
		}
		
		return "Error deleting data";
		
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
