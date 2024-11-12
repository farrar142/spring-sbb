package com.mysite.sbb.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.question.Question;




public interface CommentRepository extends JpaRepository<Comment,Integer> {
    List<Comment> findByQuestion(Question question);
    List<Comment> findByAnswer(Answer answer);
}
