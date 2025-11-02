package com.michal.openai.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.michal.openai.entity.GptResponse;

import java.util.List;

@Repository
public interface JpaGptResponseRepo extends CrudRepository<GptResponse, Integer> 
{
    @Query("SELECT content FROM GptResponse WHERE requestSlackID = ?1 ORDER BY id DESC LIMIT ?2")
    List<String> getLastResponsesToUser(String slackId, int limit);
}
