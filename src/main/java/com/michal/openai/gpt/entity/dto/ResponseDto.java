package com.michal.openai.gpt.entity.dto;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
public class ResponseDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "text")
    private String content;

    private String userSlackId;
    private String realUserName;

    private String role;

    @OneToOne
    @JoinColumn(name="request_id")
    private RequestDto requestDto;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    public ResponseDto() {}

    public ResponseDto(String content, String userSlackId, String realUserName, String role) {
        this.content = content;
        this.userSlackId = userSlackId;
        this.realUserName = realUserName;
        this.role = role;
    }
}
