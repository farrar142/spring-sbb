package com.mysite.sbb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

@SpringBootTest
class SbbApplicationTests {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private UserService userService;
    @Test
    void testJpa() throws Exception {
        SiteUser u = this.userService.getUser("sandring");
        Page<Question> ql = this.questionService.getUserQuestionList(u, 0);
        System.out.println(ql);
    }
}