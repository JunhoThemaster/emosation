package com.emosation.emosation.sevices;


import com.emosation.emosation.model.friend.FriendDTO;
import com.emosation.emosation.model.friend.Friends;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.repository.FriendRepository;
import com.emosation.emosation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Autowired
    public FriendService(FriendRepository friendRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
    }


    public List<FriendDTO> findMyFriends(User user) {
        List<Friends> frList = friendRepository.findByAddedby(user);

        return frList.stream().map(fr -> {
            // 디버깅 로그 추가
            if (fr.getYouadded() != null) {
                System.out.println("Friend's ID: " + fr.getYouadded().getId());
                System.out.println("Friend's Email: " + fr.getYouadded().getEmail());
                System.out.println("Friend's Name: " + fr.getYouadded().getName());
            } else {
                System.out.println("Youadded is null");
            }

            // FriendDTO 생성
            return new FriendDTO(
                    fr.getYouadded().getId(),
                    fr.getYouadded().getEmail(),
                    fr.getYouadded().getName()
            );
        }).collect(Collectors.toList());
    }




}
