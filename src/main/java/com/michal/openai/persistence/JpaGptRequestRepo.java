package com.michal.openai.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.michal.openai.entity.GptRequest;

@Repository
public interface JpaGptRequestRepo extends CrudRepository<GptRequest, Integer> {

}
