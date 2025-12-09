package com.michal.openai.tasksystem.entity.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@Getter
@Setter
public class TokenStore {
    private String accessToken;
    private OffsetDateTime expiresAt;

    public boolean isExpired() {
        return accessToken == null || expiresAt == null ||expiresAt.isBefore(OffsetDateTime.now().plusMinutes(2));
    }

    @Override
    public String toString() {
        return "TokenStore{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
