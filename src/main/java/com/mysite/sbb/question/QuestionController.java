package com.mysite.sbb.question;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.comment.CommentForm;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;




@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/list")
    public String list(Model model, 
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw,
            @RequestParam(value = "ordering", defaultValue = "createDate") String ordering) {
                
        
        Page<Question> paging = this.questionService.getList(page, kw,ordering);
        List<Category> categoryList = this.categoryService.getCategoryList();
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("ordering", ordering);
        model.addAttribute("category_list", categoryList);
        return "question_list";
    }
    
    @GetMapping("/category/{categoryName}")
    public String list(Model model,
            @PathVariable(value="categoryName") String categoryName,
            @RequestParam(value="page",defaultValue="0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw,
            @RequestParam(value="ordering",defaultValue="createDate") String ordering) {
        Category category = this.categoryService.getCategoryByName(categoryName);
        Page<Question> paging = this.questionService.getListByCategory(category,page, kw,ordering);
        List<Category> categoryList = this.categoryService.getCategoryList();
        paging.getContent();
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("ordering", ordering);
        model.addAttribute("category_list", categoryList);
        model.addAttribute("category", category);
        return "question_list";
    }
    
    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm,CommentForm commentForm,
            @RequestParam(value = "answerPage", defaultValue = "0") int answerPage,
            @RequestParam(value="answerOrdering",defaultValue="vote") String answerOrdering) {
        Question question = this.questionService.getQuestion(id);
        List<Category> categoryList = this.categoryService.getCategoryList();
        Page<Answer> answerPaging = this.answerService.getAnswers(question, answerPage,answerOrdering);
        model.addAttribute("question", question);
        model.addAttribute("answerPaging", answerPaging);
        model.addAttribute("answerOrdering", answerOrdering);
        model.addAttribute("category_list", categoryList);
        new Thread(new Runnable(){
            @Override
            public void run(){
                questionService.increaseViews(question);
            }
        }).start();
        
        return "question_detail";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value="/create")
    public String questionCreate(Model model,QuestionForm questionForm) {
        List<Category> categoryList = this.categoryService.getCategoryList();
        model.addAttribute("category_list",categoryList);
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value="/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        Category category = this.categoryService.getCategoryByName(questionForm.getCategory());
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, category);

        String encodedCategoryName = URLEncoder.encode(category.getName(), StandardCharsets.UTF_8);
        return String.format("redirect:/question/category/%s", encodedCategoryName);
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(Model model, QuestionForm questionForm, @PathVariable("id") Integer id,
            Principal principal) {
        
        List<Category> categoryList = this.categoryService.getCategoryList();
        model.addAttribute("category_list",categoryList);
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable Integer id, Principal principal,BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return String.format("redirect:/question/detail/%s",id);
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());

        this.questionService.vote(question, siteUser);
        return String.format("redirect:/question/detail/%s", id);
    }

}
