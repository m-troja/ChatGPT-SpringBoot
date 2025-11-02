package com.michal.openai.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.michal.openai.entity.SlackUser;

@Repository
public interface JpaSlackRepo extends CrudRepository<SlackUser, Integer> {
	
	 List<SlackUser> findAllByOrderBySlackUserId();
	 SlackUser findBySlackUserId(String slackId);
}
