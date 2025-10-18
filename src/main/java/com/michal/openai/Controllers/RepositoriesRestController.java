package com.michal.openai.Controllers;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.michal.openai.entity.StatusResponse;
import com.michal.openai.exception.UserNotFoundException;
import com.michal.openai.github.GithubService;

@RequestMapping("/v1")
@RestController
@AllArgsConstructor
public class RepositoriesRestController {
	
	private GithubService githubService;
	
	@GetMapping("/repos")
	public ResponseEntity<?> getUserRepos(@RequestParam String login) throws IOException {
	    try {
	        CompletableFuture<String> json = githubService.getUserReposWithBranches(login);
	        return ResponseEntity.ok()
	                .contentType(MediaType.APPLICATION_JSON)
	                .body(json);
	    } catch (UserNotFoundException ex) {
	        return ResponseEntity
	        		.status(HttpStatus.NOT_FOUND)
	                .body(new StatusResponse(404, ex.getMessage()));
	    }
	}
}
