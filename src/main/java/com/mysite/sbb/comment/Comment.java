package com.mysite.sbb.comment;

import java.time.LocalDateTime;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

// 재귀적 구현은 시간이 오래걸리므로 단순하게 구현
@Getter
@Setter
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate; 
    
    @ManyToOne 
    private Question question;

    @ManyToOne 
    private Answer answer;
    
    
    @ManyToOne
    private SiteUser author;

    private LocalDateTime modifyDate;

}
