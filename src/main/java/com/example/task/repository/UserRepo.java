package com.example.task.repository;

import com.example.task.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<AppUser, Long> {
    boolean existsByName(String name);
    AppUser findByName(String name);
}
