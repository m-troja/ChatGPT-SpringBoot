package com.michal.openai.persistence;

import java.sql.ResultSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

/*
 * Repository used to  extract content of last request messages by user.
 * Allows simple use of SQL limit
 */

@Slf4j
@Repository
public class RequestJdbcTemplateRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> getLastRequestsBySlackId(String slackId, int limit) 
    {
        String sql = "SELECT content FROM gpt_request WHERE author_slackid = ? ORDER BY id DESC LIMIT ?";
        String sqlDebug = "SELECT content FROM gpt_request WHERE author_slackid = " + slackId + " ORDER BY id DESC LIMIT " + limit;
        log.debug("findLastMessagesBySlackId Executing SQL: {}", sqlDebug);
        
        List<String> messages = jdbcTemplate.query(
            sql,
            ps -> {
                ps.setString(1, slackId);
                ps.setInt(2, limit);
            },
            (ResultSet rs, int rowNum) -> rs.getString("content")
        );

        log.debug("Request messages found: {}", messages.size());

        for (String msg : messages) {
            log.debug("message {}", msg);
        }

        return messages;
    }
}
