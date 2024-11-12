package com.mysite.sbb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.category.CategoryService;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.question.QuestionService;
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
    private CategoryService categoryService;

    @Autowired
    private QuestionRepository qr;
    @Autowired
    private AnswerRepository ar;
    @Test
    void testJpa() throws Exception {
        for (Integer i = 0; i<=3;i++){
            categoryService.createCategory("게시판" + i.toString());
        }
         
    }
}