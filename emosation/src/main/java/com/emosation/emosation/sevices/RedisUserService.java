package com.emosation.emosation.sevices;


import com.emosation.emosation.model.user.User;
import com.emosation.emosation.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class RedisUserService {


    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;
    private final PwService pwService;

    @Autowired
    public RedisUserService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate,EmailService emailService,PwService pwService) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
        this.pwService = pwService;

    }

    public void setBackUpPointforUser(String userEm) {
        User user = userRepository.findByEmail(userEm);

        String phKey = "deleted-user" + user.getPhone() + ":userPh:";  // 꼭 필요한 정보들만 백업포인트로서 단기 저장 하는 방식으로 하려함
                                                                        // 한 7일정도면 레디스 메모리에 저장해도 되지 않을까 싶음 db에는 이미 삭제된 사용자는 deleted-user이런식으로 변경되어있고 삭제가되진않았으니
                                                                        // db에만 영구저장 redis에서는 단기 백업포인트 pw는 복구 요청시에 새로 설정하는 방식으로 하면될듯
        String emKey = "deleted-user" + user.getPhone() + ":userEm:";
        String nameKey = "deleted-user" + user.getPhone() + ":userName:";

        redisTemplate.opsForValue().set(phKey,(user.getPhone()).toString());

        redisTemplate.opsForValue().set(emKey,userEm);
        redisTemplate.opsForValue().set(nameKey,user.getName());
    }

    @Transactional
    public boolean rollBackUser(String userEm) {
        String phKey = userEm + ":userPh:";
        String emKey = userEm + ":userEm:"; // 탈퇴한후에 사용자 찾기에서는 userEm이 delete-user{phone} 이런 식으로 설정함 delete메소드에서.
        String nameKey = userEm + ":userName:";


        User user = userRepository.findByEmail(userEm);
        System.out.println("복구시도 : " +user.getEmail());
        if(user !=null && user.getStatus().equals(User.UserStatus.DELETED)){
            String previoudEm = (String) redisTemplate.opsForValue().get(emKey);
            System.out.println(previoudEm);
            String previoudName = (String) redisTemplate.opsForValue().get(nameKey);
            String  previousPhstr = (String) redisTemplate.opsForValue().get(phKey);

            BigInteger previousPh = new BigInteger(previousPhstr);

            if(previoudEm != null){ // 어차피 저장되었던 키가 만료시에는 아무것도 존재하지않을테니 하나만 검증해도됨.
                user.setEmail(previoudEm);
                user.setName(previoudName);
                user.setStatus(User.UserStatus.INACTIVE);
                user.setPhone(previousPh);
                user.setIsAdmin(false);
                user.setPics("default.jpg");
                String pw = pwService.genTemporaryPW();

                emailService.sendMail(previoudEm,"이모세이션 임시 비밀번호" ,"임시비밀번호는 다음과 같습니다\n\n" + pw); // redis에서 추출한 탈퇴되기전 설정됐던 이메일로 임시비밀번호 전송함.

                user.setPw(BCrypt.hashpw(pw, BCrypt.gensalt()));
                userRepository.save(user);
                return true;
            }
            return false;

        }

        return false;

    }





}
