package com.example.student.security;

import com.example.student.model.User;
import com.example.student.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> "admin@bakery.local".equals(u.getEmail()));

        if (!adminExists) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@bakery.local");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);
            userRepository.save(admin);
            System.out.println(">>> Создан администратор: admin@bakery.local / admin123");
        }

        boolean userExists = userRepository.findAll().stream()
                .anyMatch(u -> "user@bakery.local".equals(u.getEmail()));

        if (!userExists) {
            User user = new User();
            user.setName("User");
            user.setEmail("user@bakery.local");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("USER");
            user.setEnabled(true);
            user.setAccountNonLocked(true);
            userRepository.save(user);
            System.out.println(">>> Создан пользователь: user@bakery.local / user123");
        }
    }
}