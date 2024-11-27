package com.emosation.emosation.sevices;


import com.emosation.emosation.model.user.UserDTO;
import com.emosation.emosation.repository.UserRepository;
import com.emosation.emosation.model.user.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;


    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean login(String em, String pw) {
        User user = userRepository.findByEmail(em);

        if (user == null) {
            return false;
        }

        if(user.getPw().equals(pw)) {
            return true;
        }

        return false;

    }

    public User getUser(String em) {
        return userRepository.findByEmail(em);
    }





    public Optional<UserDTO> findUserByName(String name) {
            Optional<User> user  = userRepository.findByName(name.trim());
            if(user.isPresent()) {
                User u = user.get();
                return Optional.of(new UserDTO(u.getId(), u.getName(),u.getEmail(),u.getPics()));
            }else{
                return Optional.empty();
            }

    }





    @Transactional
    public void save(String email, String name, String pw, BigInteger phone) {
        User user = new User();
        if(userRepository.findByEmail(email) == null) {
            System.out.println("User not found attempting to save user");

            user.setEmail(email);
            user.setName(name);
            user.setPhone(phone);
            user.setPw(pw);
            String defaultImagePath = "/img/DefaultUser.jpg";
            user.setPics(defaultImagePath);
            userRepository.save(user);
        }
        else {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }


    }



}
