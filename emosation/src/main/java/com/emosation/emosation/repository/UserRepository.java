package com.emosation.emosation.repository;

import com.emosation.emosation.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{

  User findByEmail(String email);

  Optional<User> findByName(String username);

}
