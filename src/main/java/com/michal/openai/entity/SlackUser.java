package com.michal.openai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "slack_user")  // Specify the schema name here
public class SlackUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Use Identity for MySQL auto-increment
    private int id;
    
    @Column(name = "slackid")
    private String slackId;
    
    @Column(name = "real_name")
    private String realName;

	@Override
	public String toString() {
		return "SlackUser [id=" + id + ", slackId=" + slackId + ", realName=" + realName + "]";
	}

	public SlackUser(String slackId, String realName) {
		super();
		this.slackId = slackId;
		this.realName = realName;
	}
}
