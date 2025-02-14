package com.emosation.emosation.repository;

import com.emosation.emosation.model.chat.RoomInUsers;
import com.emosation.emosation.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomInUserRepository extends JpaRepository<RoomInUsers,Long> {

    List<RoomInUsers> findByUser(User user);
}
