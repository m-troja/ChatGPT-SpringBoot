package com.michal.openai.persistence;

import com.michal.openai.gpt.entity.dto.ResponseDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseDtoRepo extends CrudRepository<ResponseDto, Integer> {

    List<ResponseDto> findByUserSlackId(String userSlackId);
}
