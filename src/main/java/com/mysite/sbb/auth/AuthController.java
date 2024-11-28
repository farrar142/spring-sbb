package com.mysite.sbb.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {
    private final CategoryService categoryService;
    private final UserService userService;
    private final KakaoService kakaoService;
    private final MailSender mailSender;
    private final Environment env;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String login(Model model) {
        List<Category> categoryList = this.categoryService.getCategoryList();
        model.addAttribute("category_list", categoryList);
        return "login_form";
    }

    @GetMapping("/signup")
    public String signup(Model model, UserCreateForm userCreateForm) {
        List<Category> categoryList = this.categoryService.getCategoryList();
        model.addAttribute("category_list",categoryList);
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }
        try {

            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        return "redirect:/";
    }

    @GetMapping("/kakao/login")
    public String kakaoLogin() {
        return "redirect:https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + env.getProperty("KAKAO_CLIENT_KEY") +
                "&redirect_uri=" + env.getProperty("KAKAO_REDIRECT_URI") +
                "&response_type=code";
    }

    @GetMapping("/kakao/callback")
    public String kakaoCallback(@RequestParam(value = "code", defaultValue = "") String code, HttpServletRequest request)
            throws JsonProcessingException {
        if (code.equals("")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "잘못된 코드입니다.");
        }
        String token = this.kakaoService.getAccessToken(code);
        KakaoInfo info = this.kakaoService.getKakaoInfo(token);
        UserService.UserContainer container = this.userService.getOrCreateKakaoUser(info);

        Authentication authentication = new UsernamePasswordAuthenticationToken(container.getUser().getUsername(), container.getPassword());
        try{

            Authentication authenticated = this.authenticationManager.authenticate(authentication);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authenticated);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            return "redirect:/";
        } catch (Exception e) {
            System.out.println(e);
            return "redirect:/";
        }
    }

    @GetMapping("/reset_password")
    public String resetPassword(Model model) {
        model.addAttribute("error", false);
        model.addAttribute("sendConfirm", false);
        model.addAttribute("email", false);
        return "reset_password";
    }

    @PostMapping("/reset_password")
    public String sendResetPasswordEmail(Model model,@RequestParam(value = "email") String email) {
        boolean error = false;
        boolean sendConfirm = true;
        try{

            SiteUser user = this.userService.getUserByEmail(email);
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(email);
            simpleMailMessage.setSubject("계정 정보입니다.");
            StringBuilder sb = new StringBuilder();

            String newPassword = UUID.randomUUID().toString().replaceAll("-","");
            sb.append(user.getUsername())
                    .append("계정의 비밀번호를 새롭게 초기화 했습니다..\n").append("새 비밀번호는 ")
                    .append(newPassword).append("입니다.\n")
                    .append("로그인 후 내 정보에서 새로 비밀번호를 지정해주세요.");
            simpleMailMessage.setText(sb.toString());
            this.userService.updatePassword(user, newPassword);

            new Thread(() -> mailSender.send(simpleMailMessage)).start();
        } catch (Exception e) {
            error = true;
            sendConfirm = false;
        }
        model.addAttribute("error", error);
        model.addAttribute("sendConfirm", sendConfirm);
        model.addAttribute("email", email);

        return "reset_password";
    }
}
