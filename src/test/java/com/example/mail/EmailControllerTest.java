package com.example.mail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.*;

@WebMvcTest(EmailController.class)
class EmailControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @Test
    void testSendEmail() throws Exception {
        doNothing().when(emailService).sendSimpleMessage(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/email/send")
                .param("to", "test@example.com")
                .param("subject", "Test Subject")
                .param("text", "Test Body"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email inviata!"));

        verify(emailService, times(1)).sendSimpleMessage("test@example.com", "Test Subject", "Test Body");
    }
}
