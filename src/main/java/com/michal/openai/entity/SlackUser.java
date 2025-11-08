package com.michal.openai.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@NoArgsConstructor
@Table(name = "slack_user")  // Specify the schema name here
public class SlackUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Use Identity for MySQL auto-increment
    private int id;

    @Setter
    @Column(name = "slackid")
    private String slackUserId;

    @Setter
    @Column(name = "real_name")
    private String slackName;

	@Override
	public String toString() {
		return "SlackUser [id=" + id + ", slackId=" + slackUserId + ", realName=" + slackName + "]";
	}

	public SlackUser(String slackUserId, String slackName) {
		super();
		this.slackUserId = slackUserId;
		this.slackName = slackName;
	}
}
