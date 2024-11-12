package com.mysite.sbb.comment;

import java.security.Principal;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import lombok.RequiredArgsConstructor;



@RequestMapping(value="/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {
    private final CommentService commentService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createComment(CommentForm commentForm, BindingResult bindingResult, Principal principal) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        Integer questionId = commentForm.getQuestionId();
        Integer answerId = commentForm.getAnswerId();
        System.out.println(commentForm);
        
        System.out.println(commentForm.getContent());
        System.out.println(commentForm.getAnswerId());
        if (questionId == null && answerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "부모의 id는 필수 값입니다.");
        }
        System.out.println("부모아이디 체크 넘김");
        Optional<Question> question = Optional.empty();
        Optional<Answer> answer = Optional.empty();

        if (!(questionId == null)) {
            question = Optional.of(this.questionService.getQuestion(questionId));
        } else {
            Answer a = this.answerService.getAnswer(answerId);
            answer = Optional.of(a);
            questionId = a.getQuestion().getId();
        }
        System.out.println("만들기 전");
        this.commentService.createComment(question, answer, siteUser, commentForm.getContent());
        return String.format("redirect:/question/detail/%s", questionId);
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String commentDelete(@PathVariable("id") Integer id, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "삭제권한이 없습니다.");
        }
        Integer questionId = 0;
        if (comment.getQuestion() != null) {
            questionId = comment.getQuestion().getId();
        } else {
            questionId = comment.getAnswer().getQuestion().getId();
        }
        this.commentService.deleteComment(comment);
        return String.format("redirect:/question/detail/%s", questionId);
    }
    
    
}
