package com.emosation.emosation.sevices;


import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PwService {


    public String genTemporaryPW(){
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialCharacters = "!@#$%^&*()_-+=<>?";

        String allCharacters = upperCaseLetters + lowerCaseLetters + digits + specialCharacters;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();


        for(int i = 0; i < 12; i++){

            int randomIndex = random.nextInt(allCharacters.length());


            password.append(allCharacters.charAt(randomIndex));
        }

        return password.toString();
    }
}
