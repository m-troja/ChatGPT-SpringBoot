package com.michal.openai.persistence;

import com.michal.openai.entity.GptMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaGptMessageRepo extends CrudRepository<GptMessage, Integer> {
}