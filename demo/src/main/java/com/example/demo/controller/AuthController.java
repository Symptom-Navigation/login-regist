package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Doctor;
import com.example.demo.security.JwtTokenUtil;
import com.example.demo.service.UserService;
import com.example.demo.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        String gender = registerRequest.get("gender");
        Integer age = Integer.parseInt(registerRequest.get("age"));

        try {
            if (userService.findByUsername(username).isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Username already taken");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setGender(gender);
            user.setAge(age);
            userService.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User registered successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed for username: {}", username, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Registration failed");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<Map<String, Object>> registerDoctor(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        String name = registerRequest.get("name");
        String department = registerRequest.get("department");
        String title = registerRequest.get("title");

        try {
            if (doctorService.findByUsername(username).isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Username already taken");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Doctor doctor = new Doctor();
            doctor.setUsername(username);
            doctor.setPassword(passwordEncoder.encode(password));
            doctor.setName(name);
            doctor.setDepartment(department);
            doctor.setTitle(title);
            doctorService.save(doctor);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Doctor registered successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Doctor registration failed for username: {}", username, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Doctor registration failed");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        try {
            logger.info("Received login request with username: {}", username);
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userService.loadUserByUsername(username);
            String token = jwtTokenUtil.generateToken(userDetails);
            logger.info("Generated JWT token for username: {}", username);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", responseData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for username: {}", username, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid username or password");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/login/doctor")
    public ResponseEntity<Map<String, Object>> loginDoctor(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        try {
            logger.info("Received login request with username: {}", username);
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = doctorService.loadUserByUsername(username);
            String token = jwtTokenUtil.generateToken(userDetails);
            logger.info("Generated JWT token for doctor username: {}", username);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", responseData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Doctor login failed for username: {}", username, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid username or password");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
