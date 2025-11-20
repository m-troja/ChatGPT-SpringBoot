package com.michal.openai.persistence;

import com.michal.openai.slack.entity.SlackUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSlackRepo extends CrudRepository<SlackUser, Integer> {
	
	 List<SlackUser> findAllByOrderBySlackUserId();
	 SlackUser findBySlackUserId(String slackId);
}
