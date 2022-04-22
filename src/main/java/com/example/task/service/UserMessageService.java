package com.example.task.service;

import com.example.task.security.JwtTokenProvider;
import com.example.task.model.AppUser;
import com.example.task.model.Message;
import com.example.task.repository.MessageRepo;
import com.example.task.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserMessageService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MessageRepo messageRepo;

    public AppUser addUser(AppUser user){
        user.setPassword(passwordEncoder.encode(user.getPassword())); // храним пароли в зашифрованном виде
        if (!userRepo.existsByName(user.getName())){
            return userRepo.save(user);
        } else {
            // тут должны быть кастомные эксепшены
            throw new RuntimeException("User: " + user.getName() + " already exists");
        }
    }

    public String giveMeToken(String name, String password) {
        AppUser appUser = userRepo.findByName(name);
        if (appUser == null) {
            throw new RuntimeException("No user with name: " + name);
        }
        if (!passwordEncoder.matches(password, appUser.getPassword())){
            throw new RuntimeException("Wrong password");
        } else {
            return jwtTokenProvider.createToken(name);
        }
    }

    public boolean checkToken(String token) {
        // Bearer token из заголовка
        return jwtTokenProvider.validateToken(token.substring("Bearer_".length()));
    }

    public Message saveUserMessageByUsername(String username, String text) {
        AppUser appUser = userRepo.findByName(username);
        if (appUser == null) {
            throw new RuntimeException("No user with name: " + username);
        }
        return messageRepo.save(new Message(null, text, appUser));
    }

    public List<Message> getLastTenMessagesFromUserByUsername(String username){
        AppUser appUser = userRepo.findByName(username);
        if (appUser == null) {
            throw new RuntimeException("No user with name: " + username);
        }
        return messageRepo.getLastTenByUserId(appUser.getId());
    }

}
