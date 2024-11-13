package com.mysite.sbb.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        String encodedPassword = this.passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        this.userRepository.save(user);
        return user;
    }

    public UserContainer getOrCreateKakaoUser(KakaoInfo info) {
        String email = info.getId()+"@kakao.com";
        Optional<SiteUser> user = this.userRepository.findByEmail(email);
        String password =  UUID.randomUUID().toString();
        if (user.isPresent()) {
            this.updatePassword(user.get(), password);
            return new UserContainer(user.get(),password);
        } else {
            String tempUserName = info.getId()+"WithKakao";
            return new UserContainer(this.create(tempUserName, email,password),password);
        }
    }

    public SiteUser getUser(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByusername(username);
        if (siteUser.isPresent()) {
            return siteUser.get();
        }
        throw new DataNotFoundException("siteuser not found");
    }

    public SiteUser getUserByEmail(String email) {
        Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
        if (siteUser.isPresent()) {
            return siteUser.get();
        }
        throw new DataNotFoundException("siteuser not found");
        
    }

    public boolean isMatchPassword(SiteUser user, String password) {
        return this.passwordEncoder.matches(password, user.getPassword());
    }

    public void updatePassword(SiteUser user, String password) {
        String encodedPassword = this.passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        this.userRepository.save(user);
    }

    @Getter
    @NoArgsConstructor
    public class UserContainer {
        private SiteUser user;
        private String password;

        public UserContainer(SiteUser user, String password) {
            this.user = user;
            this.password = password;
        }
    }
}
