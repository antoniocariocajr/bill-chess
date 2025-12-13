package com.bill.bill_chess.infra.security;

import com.bill.bill_chess.persistence.User;
import com.bill.bill_chess.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Configuration
@RequiredArgsConstructor
public class AdminUserConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        var userAdmin = userRepository.findByEmail("admin@admin.com");

        userAdmin.ifPresentOrElse(
                user -> {
                    System.out.println("admin ja existe");
                },
                () -> {
                    var user = new User();
                    user.setName("Administrador");
                    user.setEmail("admin@admin.com");
                    user.setPassword(passwordEncoder.encode("123456"));
                    user.setRoles(EnumSet.of(User.Role.ADMIN, User.Role.PLAYER));
                    userRepository.save(user);
                });
    }
}
