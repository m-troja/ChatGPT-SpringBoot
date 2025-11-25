package com.michal.openai.persistence;

import com.michal.openai.gpt.entity.dto.RequestDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestDtoRepo extends CrudRepository<RequestDto, Integer> {

    List<RequestDto> findByUserSlackId(String userSlackId);
}
