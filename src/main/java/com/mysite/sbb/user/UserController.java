package com.mysite.sbb.user;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;

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
    

}