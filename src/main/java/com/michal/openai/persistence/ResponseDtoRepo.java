package com.michal.openai.persistence;

import com.michal.openai.gpt.entity.dto.ResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseDtoRepo extends JpaRepository<ResponseDto, Integer> {

    List<ResponseDto> findByUserSlackIdOrderByTimestampDesc(String userSlackId, Pageable pageable);
}
