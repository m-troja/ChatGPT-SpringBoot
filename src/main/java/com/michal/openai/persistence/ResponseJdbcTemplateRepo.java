package com.michal.openai.persistence;

import java.sql.ResultSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ResponseJdbcTemplateRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

 
    public List<String> getLastResponsesToUser(String slackId, int limit) 
    {
        String sql = "SELECT content FROM gpt_response WHERE request_author_slackid = ? ORDER BY id DESC LIMIT ?";
        String sqlDebug = "SELECT content FROM gpt_request WHERE author_slackid = " + slackId + " ORDER BY id DESC LIMIT " + limit;
        log.info("getLastResponsesToUser Executing SQL: " + sqlDebug);
 //       System.out.println("getLastResponsesToUser Executing SQL: " + sqlDebug);
        
        List<String> messages = jdbcTemplate.query(
            sql,
            ps -> {
                ps.setString(1, slackId);
                ps.setInt(2, limit);
            },
            (ResultSet rs, int rowNum) -> rs.getString("content")
        );
        
        log.info("Response messages found: " + messages.size());
    //    System.out.println("Message found: " + messages.size());
       
        for (String msg : messages) {
  //          System.out.println("message :: " + msg);
        }

        return messages;
    }
}
