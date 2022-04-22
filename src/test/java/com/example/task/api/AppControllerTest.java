package com.example.task.api;

import com.example.task.dto.AppUserForm;
import com.example.task.dto.MessageForm;
import com.example.task.dto.TokenRequestForm;
import com.example.task.model.AppUser;
import com.example.task.model.Message;
import com.example.task.repository.MessageRepo;
import com.example.task.repository.UserRepo;
import com.example.task.service.UserMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        messageRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void shouldReturnToken() throws Exception {
        AppUser appUser =
                userMessageService.addUser(new AppUser(null, "Bill", "111", new ArrayList<>()));
        TokenRequestForm tokenRequestForm = new TokenRequestForm(appUser.getName(), "111");
        System.out.println(appUser);
        System.out.println(tokenRequestForm);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequestForm)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void shouldSaveMessageFromUser() throws Exception {
        AppUser appUser =
                userMessageService.addUser(new AppUser(null, "Bill", "111", new ArrayList<>()));
        String token = userMessageService.giveMeToken(appUser.getName(), "111");
        String testText = "this is test text";
        assertTrue(appUser.getMessages().isEmpty());
        MessageForm messageForm = new MessageForm(appUser.getName(), testText);

        mockMvc.perform(post("/message/save")
                        .header("authorization", "Bearer_"+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageForm)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.name", equalTo(appUser.getName())))
                .andExpect(jsonPath("$.message", equalTo(testText)));

        assertTrue(messageRepo.getAllByUser(appUser).stream()
                .anyMatch(message -> message.getText().equals(testText)));
    }

    @Test
    @Transactional
    void shouldGetLastTenMessagesFromUserWhenMessageIsCertainString() throws Exception {
        AppUser appUser =
                userMessageService.addUser(new AppUser(null, "Bill", "111", new ArrayList<>()));
        for(int i = 1; i <= 12; i++){
            messageRepo.save(new Message(null, i + " some text", appUser));
        }
        String token = userMessageService.giveMeToken(appUser.getName(), "111");
        String testText = "history 10";
        MessageForm messageForm = new MessageForm(appUser.getName(), testText);

        mockMvc.perform(post("/message/save")
                        .header("authorization", "Bearer_"+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageForm)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[*].text", hasItem("12 some text")));
    }

    @Test
    void shouldSaveNewUserToDatabase() throws Exception {
        AppUserForm userForm = new AppUserForm("George", "zxc");
        assertFalse(userRepo.existsByName(userForm.getUsername()));

        mockMvc.perform(post("/user/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userForm)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andExpect(jsonPath("$.name", equalTo(userForm.getUsername())));

        assertTrue(userRepo.existsByName(userForm.getUsername()));
    }
}