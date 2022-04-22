package com.example.task.api;

import com.example.task.dto.AppUserForm;
import com.example.task.dto.MessageForm;
import com.example.task.dto.TokenRequestForm;
import com.example.task.dto.TokenResponseForm;
import com.example.task.model.AppUser;
import com.example.task.model.Message;
import com.example.task.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AppController {

    private final HttpServletRequest request;
    private final UserMessageService userMessageService;

    @PostMapping("/user/login") // этот эндпоинт проверяет пароль по БД и создает jwt токен
    public ResponseEntity<TokenResponseForm> giveMeToken(@RequestBody TokenRequestForm tokenRequestForm) {
        String token = userMessageService.giveMeToken(tokenRequestForm.getUsername(), tokenRequestForm.getPassword());
        return ResponseEntity.ok().body(new TokenResponseForm(token));
    }

    @PostMapping("/message/save")
    public ResponseEntity<?> saveMessage(@RequestBody MessageForm messageForm) {
        String token = request.getHeader("authorization");
        if (userMessageService.checkToken(token)) {
            // получаем 10 последних сообщений пользователя, если message = "history 10"
            if (messageForm.getMessage().equals("history 10")) {
                List<Message> lastTenMessagesFromUserByUsername =
                        userMessageService.getLastTenMessagesFromUserByUsername(messageForm.getName());
                return ResponseEntity.ok().body(lastTenMessagesFromUserByUsername);
            }
            // в случае успешной проверки токена, сохраняем сообщение в БД
            Message message = userMessageService.saveUserMessageByUsername(messageForm.getName(), messageForm.getMessage());
            return ResponseEntity.ok().body(new MessageForm(message.getUser().getName(), message.getText()));
        }
        return ResponseEntity.badRequest().body("Invalid token");
    }

    @PostMapping("/user/save") // добавляет нового пользователя в БД
    public ResponseEntity<AppUser> saveUser(@RequestBody AppUserForm userForm) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("api/user/save").toUriString());
        AppUser appUser = new AppUser(null, userForm.getUsername(), userForm.getPassword(), null);
        return ResponseEntity.created(uri).body(userMessageService.addUser(appUser));
    }
}

