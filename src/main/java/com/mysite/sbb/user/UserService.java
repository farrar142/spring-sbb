package com.mysite.sbb.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(String username, String email, String password) {
        SiteUser user= new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        String encodedPassword = this.passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        this.userRepository.save(user);
        return  user;
    }
}
