package com.mysite.sbb.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public SiteUser create(String username, String email, String password) {
        SiteUser user= new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        this.userRepository.save(user);
        return  user;
    }
}
