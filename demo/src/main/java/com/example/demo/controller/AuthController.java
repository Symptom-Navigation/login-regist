package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Doctor;
import com.example.demo.security.JwtTokenUtil;
import com.example.demo.service.UserService;
import com.example.demo.service.DoctorService;
import org.hibernate.query.criteria.internal.expression.function.CurrentTimeFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Lob;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Lob
    byte[] avatarBytes;

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
    public ResponseEntity<Map<String, Object>> register(@RequestParam("username") String username,
                                                        @RequestParam("password") String password,
                                                        @RequestParam("gender") String gender,
                                                        @RequestParam("age") Integer age,
                                                        @RequestParam("idCardNumber") String idCardNumber,
                                                        @RequestParam("phoneNumber") String phoneNumber
                                                       /* @RequestPart("avatar") MultipartFile avatar*/) {

        Map<String, Object> response = new HashMap<>();
        //id长度
        if (!idCardNumber.matches("\\d{18}") || !phoneNumber.matches("\\d{11}")) {
            response.put("status", 400);
            response.put("message", "Invalid ID card number. Must be 18 digits.Or invalid phone number. Must be 11 digits.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
     /*   //提取byte
        try {
            avatarBytes = avatar.getBytes();
        } catch (IOException e) {
            response.put("status", 500);
            response.put("message", "Failed to regist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

      */
        //验证是否存在
        if (userService.findByUsername(username).isPresent()) {
            response.put("status", 409);
            response.put("message", "Username already taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setGender(gender);
        user.setAge(age);
    //    user.setAvatar(avatarBytes);
        user.setIdCardNumber(idCardNumber);
        user.setPhoneNumber(phoneNumber);


        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        userService.save(user);
        response.put("status", 200);
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/doctor")
    public ResponseEntity<Map<String, Object>> registerDoctor(@RequestParam("username") String username,
                                                              @RequestParam("password") String password,
                                                              @RequestParam("department") String department,
                                                              @RequestParam("title") String title,
                                                              @RequestParam("doctorname") String doctorname
                                                           /*   @RequestPart("avatar") MultipartFile avatar*/) {
    /*    try {
            avatarBytes = avatar.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

     */

        if (doctorService.findByUsername(username).isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 409);
            response.put("message", "Username already taken");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        Doctor doctor = new Doctor();
        doctor.setUsername(username);
        doctor.setDoctorName(doctorname);
        doctor.setPassword(passwordEncoder.encode(password));
        doctor.setDepartment(department);
        doctor.setTitle(title);
    //    doctor.setAvatar(avatarBytes);
        doctor.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        doctor.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        doctorService.save(doctor);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Doctor registered successfully");
        return ResponseEntity.ok(response);

    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        try {
            logger.info("Received login request with username: {}", username);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userService.loadUserByUsername(username);
            String token = jwtTokenUtil.generateToken(userDetails);
            logger.info("Generated JWT token for username: {}", username);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("data", responseData);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for username: {}", username);
            Map<String, Object> response = new HashMap<>();
            response.put("status", 401);
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Login failed for username: {}", username, e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", 500);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
            response.put("status", 200);
            response.put("data", responseData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Doctor login failed for username: {}", username, e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", 401);
            response.put("message", "Invalid username or password");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // 获取所有用户信息
    @GetMapping("/users")
    public ResponseEntity<Map<Long, User>> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            Map<Long, User> userMap = new HashMap<>();
            for (User user : users) {
                if (user.getId() != null) { // 确保键不为 null，否则将不显示改用户信息
                    userMap.put(user.getId(), user);
                }
            }
            return ResponseEntity.ok(userMap);
        } catch (Exception e) {
            logger.error("Failed to fetch all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 获取所有医生的名字、科室和级别
    @GetMapping("/doctors")
    public ResponseEntity<Map<String, Map<String, String>>> getAllDoctors() {
        try {
            List<Doctor> doctors = doctorService.findAll();
            Map<String, Map<String, String>> doctorMap = new HashMap<>();
            for (Doctor doctor : doctors) {
                if (doctor.getUsername() != null) { // 确保键不为 null,否则将不显示改用户信息
                    Map<String, String> doctorInfo = new HashMap<>();
                    doctorInfo.put("department", doctor.getDepartment());
                    doctorInfo.put("title", doctor.getTitle());
                    doctorMap.put(doctor.getUsername(), doctorInfo);
                }
            }
            return ResponseEntity.ok(doctorMap);
        } catch (Exception e) {
            logger.error("Failed to fetch all doctors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/user/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 将用户信息存入 Map
            response.put("status", 200);
            response.put("data", user);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Failed to fetch user info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/user/update-profile")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            Authentication authentication,
            @RequestBody Map<String, Object> userUpdates) {

        Map<String, Object> response = new HashMap<>();
        try {
            // 从 Spring Security 的认证对象中获取用户名
            String username = authentication.getName();

            // 根据用户名查找用户
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 更新用户信息
            if (userUpdates.containsKey("username")) {
                user.setUsername((String) userUpdates.get("username"));
            }
            if (userUpdates.containsKey("password")) {
                user.setPassword(passwordEncoder.encode((String) userUpdates.get("password")));
            }
            if (userUpdates.containsKey("gender")) {
                user.setGender((String) userUpdates.get("gender"));
            }
            if (userUpdates.containsKey("age")) {
                user.setAge((Integer) userUpdates.get("age"));
            }
            if (userUpdates.containsKey("idCardNumber")) {
                user.setIdCardNumber((String) userUpdates.get("idCardNumber"));
            }
            if (userUpdates.containsKey("phoneNumber")) {
                user.setPhoneNumber((String) userUpdates.get("phoneNumber"));
            }
        /*    if (userUpdates.containsKey("avatar")) {
                user.setAvatar((byte[]) userUpdates.get("avatar"));
            }

         */

            // 更新数据库中的用户信息
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            userService.save(user);

            response.put("status", 200);
            response.put("message", "User information updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "Failed to update user info: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
