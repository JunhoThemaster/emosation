package com.emosation.emosation.friendDTOTest;

import com.emosation.emosation.model.friend.FriendDTO;
import com.emosation.emosation.model.friend.Friends;
import com.emosation.emosation.model.user.User;
import com.emosation.emosation.repository.FriendRepository;
import com.emosation.emosation.repository.UserRepository;
import com.emosation.emosation.sevices.FriendService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class FrienddtoTEST {


//    @Mock
//    private FriendRepository friendRepository;  // FriendRepository Mock 객체
//
//    @Mock
//    private FriendDTO friendDTO;
//
//    @Mock
//    private UserRepository userRepository;      // UserRepository Mock 객체
//
//    @InjectMocks
//    private FriendService friendService;        // friendService에 userRepository, friendRepository 주입
//
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testFindMyFr(){
//        User user1 = new User();
//        BigInteger ph = new BigInteger("1259");
//
//        user1.setEmail("alice");
//        user1.setName("alice");
//        user1.setPics("unchoosed");
//        user1.setPhone(ph);
//
//
//
//        User user2 = new User();
//        BigInteger ph2 = new BigInteger("1259");
//
//        user2.setEmail("bob");
//        user2.setName("bob");
//        user2.setPics("unchoosed");
//        user2.setPhone(ph);
//
//        when(userRepository.save(user1)).thenReturn(user1);
//        when(userRepository.save(user2)).thenReturn(user2);
//        System.out.println(user1.getName());
//
//        Friends friends = new Friends();
//
//        friends.setAddedby(user1);
//        friends.setYouadded(user2);
//        friends.setAddedat(LocalDateTime.now());
//
//        List<Friends> mockedFriensList = Arrays.asList(friends);
//
//        when(friendRepository.findByAddedby(user1)).thenReturn(mockedFriensList);
//
//
//        List<FriendDTO> result = friendService.findMyFriends(user1);
//
//
//
//
//
//        assertNotNull(result);
//        System.out.println(result.toString());
//
//    }
//
//
//
//    @Test
//    public void test(){
//        Date date = new Date(System.currentTimeMillis() + 3600 *1000);
//        System.out.println(date);
//    }
//


}
