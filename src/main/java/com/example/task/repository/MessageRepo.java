package com.example.task.repository;

import com.example.task.model.AppUser;
import com.example.task.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM messages\n" +
            "WHERE user_id = :userId\n" +
            "ORDER BY id DESC LIMIT 10")
    List<Message> getLastTenByUserId(Long userId);
    List<Message> getAllByUser(AppUser user);


}
