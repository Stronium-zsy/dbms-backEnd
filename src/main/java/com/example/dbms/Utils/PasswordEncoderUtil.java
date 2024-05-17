package com.example.dbms.Utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public static String encodePassword(String password){
        return encoder.encode(password);
    }
    public static boolean matches(String rawPassword, String encodedPassword){
        return encoder.matches(rawPassword,encodedPassword);
    }






}

