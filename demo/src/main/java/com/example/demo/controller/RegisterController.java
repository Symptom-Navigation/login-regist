package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        String gender = registerRequest.get("gender");
        Integer age = registerRequest.get("age") != null ? Integer.parseInt(registerRequest.get("age")) : null;

        if (userService.findByUsername(username).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Username already exists");
            response.put("errorCode", "409");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setGender(gender);
        user.setAge(age);

        userService.save(user); // 保存用户

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User registered successfully");
        response.put("errorCode", "200");

        return ResponseEntity.ok(response);
    }
}
