package com.example.demo.service;

import com.example.demo.model.Appointment;
import com.example.demo.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> findByUserId(Long userId) {
        return appointmentRepository.findByUserId(userId);
    }

    public List<Appointment> findByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public void acceptAppointment(Long appointmentId, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Doctor ID does not match the appointment's doctor");
        }
        appointment.setStatus("Accepted");
        appointmentRepository.save(appointment);
    }

    public Optional<Appointment> findById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }
    public List<Appointment> findPendingAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, "Pending");
    }

    public void save(Appointment appointment) {
        appointmentRepository.save(appointment);
    }
}
