package com.michal.openai.persistence;

import com.michal.openai.gpt.entity.dto.RequestDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestDtoRepo extends JpaRepository<RequestDto, Integer> {

    List<RequestDto> findByUserSlackIdOrderByTimestampDesc(String userSlackId, Pageable pageable);
}
