package com.mysite.sbb.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment getComment(Integer id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent())
            return comment.get();
        throw new DataNotFoundException("comment not found");
    }
    
    public Comment createComment(Optional<Question> question, Optional<Answer> answer, SiteUser siteUser,
            String content) {
        Comment comment = new Comment();
        if (question.isPresent())
            comment.setQuestion(question.get());
        if (answer.isPresent())
            comment.setAnswer(answer.get());
        comment.setAuthor(siteUser);
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        this.commentRepository.save(comment);
        return comment;
    }
    
    public List<Comment> getCommentList(Optional<Question> question, Optional<Answer> answer) {
        if (question.isPresent()) {
            return this.commentRepository.findByQuestion(question.get());
        } else if (answer.isPresent()) {
            return this.commentRepository.findByAnswer(answer.get());
        }
        throw new DataNotFoundException("comment not found");
    }
}
