package com.michal.openai.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.michal.openai.entity.GptRequest;

import java.util.List;

@Repository
public interface JpaGptRequestRepo extends CrudRepository<GptRequest, Integer> {

    @Query("SELECT content FROM GptRequest r WHERE r.author = ?1 ORDER BY id DESC LIMIT ?2")
    List<String> getLastRequestsBySlackId(String slackId, int limit);

}
