package com.mysite.sbb.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final KakaoService kakaoService;
    private final MailSender mailSender;
    private final Environment env;
    private final AuthenticationManager authenticationManager;


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
    
    @GetMapping("/login")
    public String login(Model model) {
        List<Category> categoryList = this.categoryService.getCategoryList();
        model.addAttribute("category_list", categoryList);
        return "login_form";
    }
    
    @GetMapping("/kakao/login")
    public String kakaoLogin() {
        return "redirect:https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + env.getProperty("KAKAO_CLIENT_KEY") +
                "&redirect_uri=" + env.getProperty("KAKAO_REDIRECT_URI") +
                "&response_type=code";
    }

    @GetMapping("/kakao/callback")
    public String kakaoCallback(@RequestParam(value = "code", defaultValue = "") String code,HttpServletRequest request)
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

    private void setContents(Model model,SiteUser user,int qPage,int aPage,int cPage) {
        
        List<Category> categoryList = this.categoryService.getCategoryList();
        model.addAttribute("category_list", categoryList);
        // 리소스
        Page<Question> questionList = this.questionService.getUserQuestionList(user, qPage);
        Page<Answer> answerList = this.answerService.getUserAnswerList(user, aPage);
        Page<Comment> commentList = this.commentService.getUserCommentList(user, cPage);
        model.addAttribute("question_paging", questionList);
        model.addAttribute("answer_paging", answerList);
        model.addAttribute("comment_paging", commentList);
        model.addAttribute("user", user);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(
            UserPasswordChangeForm userPasswordChangeForm, BindingResult bindingResult,
            Model model, Principal principal,
            @RequestParam(value = "q_page", defaultValue = "0") int qPage,
            @RequestParam(value="a_page",defaultValue="0") int aPage,
            @RequestParam(value="c_page",defaultValue="0") int cPage
            ) {
        SiteUser user = this.userService.getUser(principal.getName());
        this.setContents(model, user, qPage, aPage, cPage);
        return "profile_detail";
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change_password")
    public String updateProfile(
        Model model, Principal principal,
        @RequestParam(value = "q_page", defaultValue = "0") int qPage,
        @RequestParam(value="a_page",defaultValue="0") int aPage,
        @RequestParam(value="c_page",defaultValue="0") int cPage,UserPasswordChangeForm userPasswordChangeForm, BindingResult bindingResult) {
        SiteUser user = this.userService.getUser(principal.getName());
        this.setContents(model, user, qPage, aPage, cPage);
        System.out.println(userPasswordChangeForm.getOriginPassword());
        System.out.println(userPasswordChangeForm.getPassword1());
        System.out.println(userPasswordChangeForm.getPassword2());
        if (bindingResult.hasErrors()) {
            return "profile_detail";
        }
        if (!this.userService.isMatchPassword(user, userPasswordChangeForm.getOriginPassword())) {
            bindingResult.rejectValue("originPassword", "passwordInCorrect", "기존 패스워드가 일치하지 않습니다.");
        }
        if (!userPasswordChangeForm.getPassword1().equals(userPasswordChangeForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordNotMatched", "확인 패스워드가 일치하지 않습니다.");
        }
        if (bindingResult.hasErrors()) {
            return "profile_detail";
        }
        this.userService.updatePassword(user,userPasswordChangeForm.getPassword1());
        return "profile_detail";
    }
    @GetMapping("/reset_password")
    public String resetPassword(Model model) {
        model.addAttribute("error", false);
        model.addAttribute("sendConfirm", false);
        model.addAttribute("email", false);
        return "reset_password";
    }

    @PostMapping("/reset_password")
    public String sendResetPasswordEmail(Model model, @RequestParam(value = "email") String email) {
        model.addAttribute("error", false);
        model.addAttribute("sendConfirm", true);
        model.addAttribute("email", email);
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
            return "reset_password";
        } catch (Exception e) {
            model.addAttribute("error", true);
            model.addAttribute("sendConfirm", false);
            model.addAttribute("email", email);
            return "reset_password";
        }

    }
}