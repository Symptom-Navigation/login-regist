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

import java.sql.Date;
import java.sql.Time;
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
    public ResponseEntity<Map<String, Object>> createAppointment(@RequestBody Map<String, String> appointmentRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.parseLong(appointmentRequest.get("userId"));
            Long doctorId = Long.parseLong(appointmentRequest.get("doctorId"));
            Date appointmentDate = Date.valueOf(appointmentRequest.get("appointmentDate"));
            Time appointmentTime = Time.valueOf(appointmentRequest.get("appointmentTime"));

            User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Doctor doctor = doctorService.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));

            Appointment appointment = new Appointment();
            appointment.setUser(user);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setAppointmentTime(appointmentTime);
            appointment.setStatus("Pending");

            appointmentService.save(appointment);

            response.put("status", "success");
            response.put("message", "Appointment created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // 获取用户的预约
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByUserId(@PathVariable Long userId) {
        List<Appointment> appointments = appointmentService.findByUserId(userId);
        return ResponseEntity.ok(appointments);
    }

    // 获取医生的预约
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable Long doctorId, Authentication authentication) {
        String username = authentication.getName();
        Doctor doctor = doctorService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!doctor.getId().equals(doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Appointment> appointments = appointmentService.findByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    // 接受预约
    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptAppointment(@RequestParam Long appointmentId, Authentication authentication) {
        String username = authentication.getName();
        Doctor doctor = doctorService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        appointmentService.acceptAppointment(appointmentId, doctor.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Appointment accepted");

        return ResponseEntity.ok(response);
    }

    // 获取待处理的预约
    @GetMapping("/pending")
    public ResponseEntity<List<Appointment>> getPendingAppointments(Authentication authentication) {
        String username = authentication.getName();
        Doctor doctor = doctorService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Appointment> appointments = appointmentService.findPendingAppointmentsByDoctorId(doctor.getId());
        return ResponseEntity.ok(appointments);
    }
}
