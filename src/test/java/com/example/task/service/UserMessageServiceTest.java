package com.example.task.service;

import com.example.task.model.AppUser;
import com.example.task.model.Message;
import com.example.task.repository.MessageRepo;
import com.example.task.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserMessageServiceTest {

    @Autowired
    UserRepo userRepo;

    @Autowired
    MessageRepo messageRepo;

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        messageRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void shouldAddNewUserToDatabase() {
        AppUser newUser = new AppUser(null, "Jack", "password", new ArrayList<>());
        assertFalse(userRepo.existsByName(newUser.getName()));

        userMessageService.addUser(newUser);

        assertTrue(userRepo.existsByName(newUser.getName()));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAddExistingUser() {
        AppUser appUser = userRepo.save(new AppUser(null, "Jack", "password", new ArrayList<>()));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userMessageService.addUser(appUser));

        assertEquals("User: " + appUser.getName() + " already exists", exception.getMessage());
    }

    @Test
    void shouldThrowRunTimeExceptionWhenGiveMeTokenWithWrongUsername() {
        String name = "No such name should be in repository";
        assertFalse(userRepo.existsByName(name));

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> userMessageService.giveMeToken(name, "text"));

        assertEquals("No user with name: " + name, exception.getMessage());
    }

    @Test
    void shouldThrowRunTimeExceptionWhenGiveMeTokenWithWrongPassword() {
        AppUser appUser = userRepo.save(new AppUser(null, "Jack", "password", new ArrayList<>()));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userMessageService.giveMeToken(appUser.getName(), "wrong"));

        assertEquals("Wrong password", exception.getMessage());
    }

    @Test
    @Transactional
    void shouldSaveUserMessageByUsername() {
        AppUser appUser = new AppUser(null, "testName", "test", new ArrayList<>());
        userRepo.save(appUser);
        String name = appUser.getName();

        Message message = userMessageService.saveUserMessageByUsername(name, "This is a test text for message");

        assertTrue(messageRepo.existsById(message.getId()));
        assertEquals(messageRepo.getById(message.getId()).getUser().getName(), name);
    }

    @Test
    void shouldThrowNewRuntimeExceptionWhenSaveMessageByUsernameWithWrongUsername() {
        String name = "No such name should be in repository";
        assertFalse(userRepo.existsByName(name));

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> userMessageService.saveUserMessageByUsername(name, "text"));

        assertEquals("No user with name: " + name, exception.getMessage());
    }

    @Test
    @Transactional
    void shouldGetLastTenMessagesFromUser() {
        AppUser savedUser = userRepo.save(new AppUser(null, "testName", "test", new ArrayList<>()));
        for (int i = 1; i <= 15; i++) {
            messageRepo.save(new Message(null, i + "", savedUser));
        }

        List<Message> lastTenMessagesFromUser =
                userMessageService.getLastTenMessagesFromUserByUsername(savedUser.getName());

        assertEquals(10, lastTenMessagesFromUser.size());
        assertFalse(lastTenMessagesFromUser.stream().anyMatch(message -> message.getText().equals(5 + "")));
    }

    @Test
    @Transactional
    void shouldGetLastFiveMessagesFromUser() {
        AppUser savedUser = userRepo.save(new AppUser(null, "testName", "test", new ArrayList<>()));
        for (int i = 1; i <= 5; i++) {
            messageRepo.save(new Message(null, i + "", savedUser));
        }

        List<Message> lastTenMessagesFromUser =
                userMessageService.getLastTenMessagesFromUserByUsername(savedUser.getName());

        assertEquals(5, lastTenMessagesFromUser.size());
    }

    @Test
    void shouldThrowNewRuntimeExceptionWhenGetLastTenMessagesByUsernameWithWrongUsername() {
        String name = "No such name should be in repository";
        assertFalse(userRepo.existsByName(name));

        RuntimeException exception =
                assertThrows(RuntimeException.class, () ->
                        userMessageService.getLastTenMessagesFromUserByUsername(name));

        assertEquals("No user with name: " + name, exception.getMessage());
    }
}