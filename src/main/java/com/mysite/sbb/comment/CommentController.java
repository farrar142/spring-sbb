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
        Optional<Integer> questionId = commentForm.getQuestionId();
        Optional<Integer> answerId = commentForm.getAnswerId();
        if (questionId.isEmpty() && answerId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "부모의 id는 필수 값입니다.");
        } else if (questionId.isPresent() && answerId.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer 혹은 Question 한쪽의 id가 필요합니다.");
        }
        Optional<Question> question = Optional.empty();
        Optional<Answer> answer = Optional.empty();

        if (questionId.isPresent()) {
            question = Optional.of(this.questionService.getQuestion(questionId.get()));
        } else {
            answer = Optional.of(this.answerService.getAnswer(answerId.get()));
        }
        this.commentService.createComment(question, answer, siteUser, commentForm.getContent());
        return new String();
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
