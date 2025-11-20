package com.michal.openai.controllers;

import com.michal.openai.slack.SlackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.mockito.Mockito.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @MockitoBean
    private SlackService slackService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnOkAndReturnListOfUsers() throws Exception {
        when(slackService.getAllSlackUsers()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/users/all"))
                        .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        verify(slackService, times(1)).getAllSlackUsers();
    }
}
