package com.mysite.sbb.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.user.SiteUser;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public Page<Question> getList(int page, String kw,String ordering) {
        List<Sort.Order> sorts = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        if (ordering.equals("latestAnswer")) {
            return this.questionRepository.findQuestionsByKeywordWithLatestAnswerDate(kw, pageable);
        }else if (
            ordering.equals("latestComment")
        ){
            return this.questionRepository.findQuestionsByKeywordWithLatestCommentDate(kw, pageable);
        }
        sorts.add(Sort.Order.desc("createDate"));
        return this.questionRepository.findAllByKeyword(kw, pageable);
    }

    public Page<Question> getCategoryQuestionList(Category category, int page, String kw,String ordering) {
        List<Sort.Order> sorts = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        if (ordering.equals("latestAnswer")) {
            return this.questionRepository.findQuestionsByKeywordAndCategoryWithLatestAnswerDate(category.getName(), kw, pageable);
        }else if (
            ordering.equals("latestComment")
        ){
            return this.questionRepository.findQuestionsByKeywordAndCategoryWithLatestCommentDate(category.getName(), kw, pageable);
        }
        sorts.add(Sort.Order.desc("createDate"));
        return this.questionRepository.findAllByKeywordAndCategory(category.getName(), kw, pageable);
    }
    

    public Page<Question> getUserQuestionList(SiteUser user, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.questionRepository.findByAuthor(user, pageable);
    }
    
    
    public Question getQuestion(Integer id) {
        Optional<Question> question = this.questionRepository.findById(id);
        if (question.isPresent()) {
            return question.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public Question create(String subject, String content, SiteUser siteUser,Category category) {
        Question question = new Question();
        question.setSubject(subject);
        question.setContent(content);
        question.setCreateDate(LocalDateTime.now());
        question.setAuthor(siteUser);
        question.setCategory(category);
        this.questionRepository.save(question);
        return question;
    }
    
    public Question modify(Question question, String subject, String content) {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        this.questionRepository.save(question);
        return question;
    }

    public void delete(Question question) {
        this.questionRepository.delete(question);
    }

    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser);
        this.questionRepository.save(question);
    }

    public void increaseViews(Question question) {
        Integer views = 0;
        if (question.getViews() == null) {
            views += 1;
        } else {
            views = question.getViews() + 1;
        }
        question.setViews(views);
        this.questionRepository.save(question);
    }


    private Specification<Question> search(String kw) {
        String searchKw = "%"+kw+"%";
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복을 제거 
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                return cb.or(
                    cb.like(q.get("subject"),searchKw), // 제목 
                    cb.like(q.get("content"),searchKw),
                    cb.like(u1.get("username"), searchKw),
                    cb.like(a.get("content"),searchKw),
                    cb.like(u2.get("username"), searchKw));
            }
        };
    }
}
