package com.emosation.emosation.sevices;


import com.emosation.emosation.model.chat.RoomInUsers;
import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.repository.ChatRepository;
import com.emosation.emosation.repository.RoomInUserRepository;
import com.emosation.emosation.repository.UserRepository;
import com.emosation.emosation.model.user.User;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final RoomInUserRepository roomInUserRepository;
    private final Object lock = new Object();
    private final RedisTemplate<String,Object> redisTemplate;

    @Autowired
    public UserService(UserRepository userRepository, ChatRepository chatRepository, RoomInUserRepository roomInUserRepository,RedisTemplate<String,Object> redisTemplate) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.roomInUserRepository = roomInUserRepository;
        this.redisTemplate = redisTemplate;

    }


    public boolean login(String em, String pw) {
        User user = userRepository.findByEmail(em);

        if (user == null ) {
            return false;
        }

        if(BCrypt.checkpw(pw,user.getPw())) {
            return true;
        }
        return false;

    }

    public boolean isAdmin(String em){
        User user = userRepository.findByEmail(em);

        if(user.getIsAdmin()){
            return true;
        }

        return false;

    }

    public User getUser(String em) {
        return userRepository.findByEmail(em);
    }

    public UserDTO convertToUserDto(String em){
        User user = userRepository.findByEmail(em);
        UserDTO userDTO = null;
        if(user != null){
            userDTO = new UserDTO(user.getId(),user.getName(),user.getEmail(),user.getPics(),(user.getRegisterd_at()).toString(),user.getStatus());
        }

        return userDTO;
    }


    public List<UserDTO> getAllUsers(){
        List<User> users = userRepository.findAll();


        List<UserDTO> userDTOs = new ArrayList<>();
        if(!users.isEmpty()){

            for (User user : users) {

                UserDTO userDTO = new UserDTO(user.getId(),user.getName(),user.getEmail(),user.getPics(),user.getRegisterd_at().toString(),user.getStatus());

                userDTOs.add(userDTO);
            }
            return userDTOs;
        }

        return userDTOs;

    }


    public Page<UserDTO> getUsers(Pageable pageable){
        return userRepository.findAll(pageable).map(user -> new UserDTO(user.getId(),user.getName(),user.getEmail(),user.getPics(),user.getRegisterd_at().toString(),user.getStatus()));
    }





    public Optional<UserDTO> findUserByEmail(String userEmail) {
            Optional<User> user  = Optional.ofNullable(userRepository.findByEmail(userEmail));


            if(user.isPresent()) {
                User u = user.get();
                return Optional.of(new UserDTO(u.getId(), u.getName(),u.getEmail(),u.getPics(),(u.getRegisterd_at()).toString(),u.getStatus()));
            }else{
                return Optional.empty();
            }

    }

    @Transactional
    public boolean updateUser(String em,String pw){
        User user = userRepository.findByEmail(em);
        if(user != null) {
            String encrptPw = BCrypt.hashpw(pw, BCrypt.gensalt());
            user.setPw(encrptPw);
            user.setStatus(User.UserStatus.ACTIVE);
            userRepository.save(user);
            return true;
        }
        return false;
    }






    @Transactional
    public void save(String email, String name, String pw, BigInteger phone) {

        String lockKey = "LCK_USEREM:"+email; // 가입 요청을 하게되면 해당 이메일에 대한 키를 설정하고 밸류는 lock으로 설정.. 5초동안.
        boolean validate = false;
        // 동시성 문제에있어서 synchronize 블럭과 테이블 email필드에 unique 제약을 걸었음에도 동시가입시에 2명이 같이 삽입되었음.
        // 트랜잭션 레벨에 있어서 발생하는 동시성 문제는 해결이 안되는듯 보임.. 따라서 Redis 분산 락을 써서 해결함.

        try {
            validate = redisTemplate.opsForValue().setIfAbsent(lockKey,"lock", Duration.ofSeconds(5)); // 여기서 값이 해당 이메일의 키가 존재한다면 false가 반환됨

            if(!validate){
               throw new IllegalStateException("다른 요청이 이 이메일을 처리중"); // false인 경우 이미 가입 진행중인 이메일이 있다는 뜻임.

            }
            User user1 = userRepository.findByEmail(email);
            // else
            if (user1 != null) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }

            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPhone(phone);
            String encryptedPw = BCrypt.hashpw(pw, BCrypt.gensalt());
            user.setPw(encryptedPw);
            user.setStatus(User.UserStatus.ACTIVE);
            user.setIsAdmin(false);
            user.setPics("/img/DefaultUser.jpg");
            userRepository.save(user);

        }finally {
            if(validate){ // 최종적으로 해당 키를 삭제.
                redisTemplate.delete(lockKey);
            }
        }


    }



    @Transactional
    public boolean deleteUser(String userEm){

        User user = userRepository.findByEmail(userEm);
        if(user != null){
            user.setStatus(User.UserStatus.DELETED);
            user.setEmail("deleted-user" + user.getPhone());
            user.setPw("deleted-user");
            user.setPics("/img/DefaultUser.jpg");
            user.setName("deleted-user" + user.getPhone());
            userRepository.save(user);
            return true;
        }

        return false;

    }



}
