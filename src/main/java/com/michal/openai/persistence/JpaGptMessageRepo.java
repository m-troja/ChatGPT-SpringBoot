package com.michal.openai.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class JpaGptMessageRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

 
}