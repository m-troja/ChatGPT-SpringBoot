package com.michal.openai.persistence;

import java.sql.ResultSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

/*
 * Repository used to  extract content of last response messages to user.
 * Allows simple use of SQL limit
 */

@Slf4j
@Repository
public class ResponseJdbcTemplateRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

 
    public List<String> getLastResponsesToUser(String slackId, int limit) 
    {
        String sql = "SELECT content FROM gpt_response WHERE request_author_slackid = ? ORDER BY id DESC LIMIT ?";
        String sqlDebug = "SELECT content FROM gpt_request WHERE author_slackid = " + slackId + " ORDER BY id DESC LIMIT " + limit;
        log.debug("getLastResponsesToUser Executing SQL: " + sqlDebug);

        List<String> messages = jdbcTemplate.query(
            sql,
            ps -> {
                ps.setString(1, slackId);
                ps.setInt(2, limit);
            },
            (ResultSet rs, int rowNum) -> rs.getString("content")
        );
        
        log.debug("Response messages found: " + messages.size());
        for (String message : messages) {
        	log.debug("message : " + message);
        }

        return messages;
    }
}
