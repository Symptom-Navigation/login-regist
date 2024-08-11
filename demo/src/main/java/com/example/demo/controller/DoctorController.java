package com.example.demo.controller;

import com.example.demo.model.Doctor;
import com.example.demo.model.Appointment;
import com.example.demo.service.DoctorService;
import com.example.demo.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<Doctor>> getDoctorsByDepartment(@RequestParam String department) {
        List<Doctor> doctors = doctorService.findByDepartment(department);
        return ResponseEntity.ok(doctors);
    }

    // 获取待处理预约
    @GetMapping("/appointments/pending")
    public ResponseEntity<List<Appointment>> getPendingAppointments(Authentication authentication) {
        String username = authentication.getName();
        Doctor doctor = doctorService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Appointment> appointments = appointmentService.findPendingAppointmentsByDoctorId(doctor.getId());
        return ResponseEntity.ok(appointments);
    }

    // 接受预约
    // 接受预约
    @PostMapping("/appointments/accept")
    public ResponseEntity<Map<String, Object>> acceptAppointment(@RequestBody Map<String, Object> requestBody, Authentication authentication) {
        String username = authentication.getName();
        Doctor doctor = doctorService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Long appointmentId = Long.parseLong(requestBody.get("appointmentId").toString());
        Long userId = Long.parseLong(requestBody.get("userId").toString());

        // 获取医生的所有预约
        List<Appointment> appointments = appointmentService.findByDoctorId(doctor.getId());

        // 返回所有预约信息
        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointments);

        // 查找并更新指定 user_id 的预约状态
        Appointment appointment = appointmentService.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (appointment.getUser().getId().equals(userId)) {
            appointment.setStatus("Accepted");
            appointmentService.save(appointment);

            response.put("status", "success");
            response.put("message", "Appointment accepted");
        } else {
            response.put("status", "error");
            response.put("message", "Invalid user ID for the selected appointment");
        }

        return ResponseEntity.ok(response);
    }
}
