package com.michal.openai.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.michal.openai.entity.GptMessage;
import com.michal.openai.entity.GptResponse;

@Repository
public interface JpaGptResponseRepo extends CrudRepository<GptResponse, Integer> 
{

}
