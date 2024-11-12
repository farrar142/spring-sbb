package com.mysite.sbb;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

@SpringBootTest
class SbbApplicationTests {

    @Autowired
    private CommentService commentService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private UserService userService;

    @Autowired
    private QuestionRepository qr;
    @Autowired
    private AnswerRepository ar;
    @Test
    void testJpa() throws Exception {
        List<Answer> answerList = ar.findAll();
        Answer answer = answerList.get(answerList.size()-1);
        SiteUser user = userService.getUser("sandring");

        this.commentService.createComment(Optional.empty(), Optional.of(answer), user, "Content");
         
    }
}