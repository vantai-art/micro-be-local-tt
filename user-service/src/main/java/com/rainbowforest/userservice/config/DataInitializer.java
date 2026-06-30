package com.rainbowforest.userservice.config;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.entity.UserDetails;
import com.rainbowforest.userservice.entity.UserRole;
import com.rainbowforest.userservice.repository.UserRepository;
import com.rainbowforest.userservice.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private UserRoleRepository roleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureRole("ROLE_ADMIN");
        ensureRole("ROLE_STAFF");
        ensureRole("ROLE_USER");
        if (userRepo.findByUserName("admin") == null) {
            createUser("admin", "admin123", "ROLE_ADMIN", "Admin", "HeThong", "vantai909zk@gmail.com");
            log.info("Created admin account: admin / admin123");
        }
        if (userRepo.findByUserName("staff") == null) {
            createUser("staff", "staff123", "ROLE_STAFF", "Nhan", "Vien", "staff@shop.com");
            log.info("Created staff account: staff / staff123");
        }
    }

    private UserRole ensureRole(String name) {
        UserRole r = roleRepo.findUserRoleByRoleName(name);
        if (r == null) {
            r = new UserRole();
            r.setRoleName(name);
            r = roleRepo.save(r);
        }
        return r;
    }

    private void createUser(String username, String password, String roleName,
            String firstName, String lastName, String email) {
        UserDetails d = new UserDetails();
        d.setFirstName(firstName);
        d.setLastName(lastName);
        d.setEmail(email);
        User u = new User();
        u.setUserName(username);
        // ✅ Hash password bằng BCrypt thay vì lưu plain text
        u.setUserPassword(passwordEncoder.encode(password));
        u.setActive(1);
        u.setUserDetails(d);
        u.setRole(ensureRole(roleName));
        userRepo.save(u);
    }
}