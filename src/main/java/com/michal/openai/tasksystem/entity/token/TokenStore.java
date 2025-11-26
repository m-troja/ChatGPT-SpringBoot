package com.michal.openai.tasksystem.entity.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Getter
@Setter
public class TokenStore {
    private String accessToken;
    private Instant expiresAt;

    public boolean isExpired() {
        return accessToken == null || expiresAt == null || Instant.now().isAfter(expiresAt);
    }

    @Override
    public String toString() {
        return "TokenStore{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
