package com.emosation.emosation.Util;

import java.security.SecureRandom;
import java.util.Base64;

public class Keygen {

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        String SecretKey = Base64.getEncoder().encodeToString(key);
        System.out.println(SecretKey);
    }
}
