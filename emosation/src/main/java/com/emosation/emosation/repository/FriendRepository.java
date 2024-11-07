package com.emosation.emosation.repository;

import com.emosation.emosation.model.friend.Friends;
import com.emosation.emosation.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friends,Long> {

    List<Friends> findByAddedby(User user);
}
