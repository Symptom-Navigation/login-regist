package com.example.demo.service;

import com.example.demo.model.Doctor;
import com.example.demo.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService implements UserDetailsService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Doctor> doctor = doctorRepository.findByUsername(username);
        if (doctor.isEmpty()) {
            throw new UsernameNotFoundException("Doctor not found");
        }
        return new org.springframework.security.core.userdetails.User(doctor.get().getUsername(), doctor.get().getPassword(), new ArrayList<>());
    }
    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }
    public Optional<Doctor> findByUsername(String username) {
        return doctorRepository.findByUsername(username);
    }

    public List<Doctor> findByDepartment(String department) {
        return doctorRepository.findByDepartment(department);
    }

    public void save(Doctor doctor) {
        doctorRepository.save(doctor);
    }
    public List<Doctor> findAllDoctors() {
        return doctorRepository.findAll();
    }


}
