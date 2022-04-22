package com.example.task.repository;

import com.example.task.model.AppUser;
import com.example.task.model.Message;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageRepoTest {

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    void clean() {
        userRepo.deleteAll();
        messageRepo.deleteAll();
    }

    @Test
    @Transactional
    void getLastTenByUserId() {
        AppUser appUser = userRepo.save(new AppUser(null, "Andrey", "4444", new ArrayList<>()));
        for (int i = 1; i <= 12; i++) {
            messageRepo.save(new Message(null, i + " test text", appUser));
        }

        List<Message> tenMessages = messageRepo.getLastTenByUserId(appUser.getId());

        assertEquals(10, tenMessages.size());
        assertFalse(tenMessages.stream().anyMatch(message -> message.getText().equals(2 + " test text")));
    }
}