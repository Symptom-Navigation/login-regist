package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.model.Doctor;
import com.example.demo.model.User;
import com.example.demo.service.AppointmentService;
import com.example.demo.service.DoctorService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    // 创建预约
    @PostMapping("/appointments")
    public ResponseEntity<Map<String, Object>> createAppointment(
            @RequestParam("userName") String userName,
            @RequestParam("doctorName") String doctorName,
            @RequestParam("appointmentDate") String appointmentDateStr,
            @RequestParam("appointmentTime") String appointmentTimeStr,
            @RequestParam("description") String description,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @RequestPart(value = "image3", required = false) MultipartFile image3) {

        Map<String, Object> response = new HashMap<>();
        try {
            Date appointmentDate = Date.valueOf(appointmentDateStr);
            Time appointmentTime = Time.valueOf(appointmentTimeStr);

            User user = userService.findByUsername(userName)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Doctor doctor = doctorService.findByUsername(doctorName)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            Appointment appointment = new Appointment();
            appointment.setUser(user);
            appointment.setDoctor(doctor);
            appointment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            appointment.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            appointment.setAppointmentDate(appointmentDate);
            appointment.setAppointmentTime(appointmentTime);
            appointment.setStatus("Pending");
            appointment.setDescription(description);

            if (image1 != null) {
                appointment.setImage1(image1.getBytes());
            }
            if (image2 != null) {
                appointment.setImage2(image2.getBytes());
            }
            if (image3 != null) {
                appointment.setImage3(image3.getBytes());
            }

            appointmentService.save(appointment);

            response.put("status", 201);
            response.put("message", "Appointment created successfully");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("status", 409);
            response.put("message", "Failed to create appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }


    // 获取用户的预约
    @GetMapping("/user/QueryAppointment")
    public ResponseEntity<?> getAppointmentByUserId(Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
            Long userId = user.getId();
            List<Appointment> appointments = appointmentService.findByUserId(userId);
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to Query: " + e.getMessage());
            response.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 获取医生的预约
    @GetMapping("/doctor/QueryAppointment")
    public ResponseEntity<?> getAppointmentsByDoctorId(Authentication authentication) {
        try {
            Doctor doctor = doctorService.findByUsername(authentication.getName()).orElseThrow(() -> new RuntimeException("Doctor not found"));
            Long doctorId = doctor.getId();
            List<Appointment> appointments = appointmentService.findByDoctorId(doctorId);
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Failed to Query: " + e.getMessage());
            response.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // 接受预约
    @PostMapping("/doctor/accept/{appointmentId}")
    public ResponseEntity<Map<String, Object>> acceptAppointment(@PathVariable Long appointmentId, Authentication authentication) {
        try {
            //检查医生是否存在
            String doctorName = authentication.getName();
            Doctor doctor = doctorService.findByUsername(doctorName)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            // 检查预约ID是否存在
            Appointment appointment = appointmentService.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            // 处理接受预约的逻辑
            appointmentService.acceptAppointment(appointment.getId(), doctor.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Appointment accepted");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 404);
            response.put("message", "Failed to accept: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    //取消预约
    @PostMapping("/doctor/cancel/{appointmentId}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Long appointmentId, Authentication authentication) {
        try {
            // 检查医生是否存在
            String doctorName = authentication.getName();
            Doctor doctor = doctorService.findByUsername(doctorName)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            // 检查预约ID是否存在
            Appointment appointment = appointmentService.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found"));

            // 处理取消预约的逻辑
            appointmentService.cancelAppointment(appointment.getId(), doctor.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Appointment canceled");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 404);
            response.put("message", "Failed to cancel: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    // 获取待处理的预约
    @GetMapping("/doctor/pending")
    public ResponseEntity<?> getPendingAppointments(Authentication authentication) {
        try {
            String doctorName = authentication.getName();
            Doctor doctor = doctorService.findByUsername(doctorName)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));
            List<Appointment> appointments = appointmentService.findPendingAppointmentsByDoctorId(doctor.getId());
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", 404);
            response.put("message", "Failed to query pending: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/QueryAllDoctors")
    public ResponseEntity<?> getAllDoctors(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        // 确保用户已经认证
        try {
            User user = userService.findByUsername(authentication.getName()).
                    orElseThrow(() -> new RuntimeException("User not found"));
            List<Doctor> doctors = doctorService.findAllDoctors();
            response.put("status", 200);
            response.put("doctors", doctors);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("status", 404);
            response.put("message", "Failed to found: ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
