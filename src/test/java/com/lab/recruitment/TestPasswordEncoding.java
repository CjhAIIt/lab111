package com.lab.recruitment;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordEncoding {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String hashedPassword = "$2a$10$7JB720yubVSOfvVWbfXCOOxjTOQcQjmrJF1ZM4nAVccp/.rkMlDWy";
        
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Hashed password: " + hashedPassword);
        System.out.println("Matches: " + encoder.matches(rawPassword, hashedPassword));
        
        // 生成一个新的哈希来比较
        String newHash = encoder.encode(rawPassword);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(rawPassword, newHash));
    }
}