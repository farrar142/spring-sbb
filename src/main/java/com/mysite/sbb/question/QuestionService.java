package com.mysite.sbb.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mysite.sbb.comment.Comment;
import jakarta.persistence.criteria.*;
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

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public Page<Question> getList(int page, String kw,String ordering) {
        Pageable pageable = PageRequest.of(page, 10);
        return this.questionRepository.findAll(search(kw,Optional.empty(),Optional.empty(),ordering),pageable);
//        if (ordering.equals("latestAnswer")) {
//            return this.questionRepository.findQuestionsByKeywordWithLatestAnswerDate(kw, pageable);
//        }else if (
//            ordering.equals("latestComment")
//        ){
//            return this.questionRepository.findQuestionsByKeywordWithLatestCommentDate(kw, pageable);
//        }
//        sorts.add(Sort.Order.desc("createDate"));
//        return this.questionRepository.findAllByKeyword(kw, pageable);
    }

    public Page<Question> getListByCategory(Category category, int page, String kw,String ordering) {
        Pageable pageable = PageRequest.of(page, 10);
        return this.questionRepository.findAll(search(kw,Optional.empty(),Optional.of(category),ordering),pageable);
//        if (ordering.equals("latestAnswer")) {
//            return this.questionRepository.findQuestionsByKeywordAndCategoryWithLatestAnswerDate(category.getName(), kw, pageable);
//        }else if (
//            ordering.equals("latestComment")
//        ){
//            return this.questionRepository.findQuestionsByKeywordAndCategoryWithLatestCommentDate(category.getName(), kw, pageable);
//        }
//        sorts.add(Sort.Order.desc("createDate"));
//        return this.questionRepository.findAllByKeywordAndCategory(category.getName(), kw, pageable);
    }
    

    public Page<Question> getUserQuestionList(SiteUser user, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, 10);
        return this.questionRepository.findAll(search("",Optional.of(user),Optional.empty(),""),pageable);
//        return this.questionRepository.findByAuthor(user, pageable);
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


    private Specification<Question> search(String kw,Optional<SiteUser> author,Optional<Category> category,String ordering) {
        String searchKw = "%"+kw+"%";
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
//                query.distinct(true);  // 중복을 제거
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
                Predicate conjunction = cb.conjunction();
                if (!kw.isEmpty())conjunction = cb.and(conjunction,cb.or(
                            cb.like(q.get("subject"),searchKw), // 제목
                            cb.like(q.get("content"),searchKw),
                            cb.like(u1.get("username"), searchKw),
                            cb.like(a.get("content"),searchKw),
                            cb.like(u2.get("username"), searchKw)));
                conjunction = cb.and(conjunction,this.findByAuthor(q,query,cb));
                conjunction = cb.and(conjunction,this.findByCategory(q,query,cb));
                if (ordering.equals("latestComment")){this.orderByLatestComment(q,query,cb);}
                else if (ordering.equals("latestAnswer")){this.orderByLatestAnswer(q,query,cb);}
                else {this.orderByCreateDate(q,query,cb);}
                return conjunction;
            }
            private Predicate findByAuthor(Root<Question> q,CriteriaQuery<?>query,CriteriaBuilder cb){
                return author.map(a -> cb.equal(q.get("author"), a))
                        .orElse(cb.conjunction());
            }
            private Predicate findByCategory(Root<Question> q,CriteriaQuery<?>query,CriteriaBuilder cb){
                return category.map(a -> cb.equal(q.get("category"), a))
                        .orElse(cb.conjunction());
            }
            private void orderByLatestAnswer(Root<Question> q,CriteriaQuery<?>query,CriteriaBuilder cb){
                Subquery<LocalDateTime> subquery = query.subquery(LocalDateTime.class);
                Root<Answer> subRoot = subquery.from(Answer.class);
                Expression<LocalDateTime> createDateExpression = subRoot.get("createDate");
                subquery.select(cb.greatest(createDateExpression))
                        .where(cb.equal(subRoot.get("question"),q));
                Expression<LocalDateTime> subqueryExpression = cb.coalesce(subquery,
                        q.get("createDate")
                );
                query.orderBy(cb.desc(subqueryExpression));
            }
            private void orderByLatestComment(Root<Question> q,CriteriaQuery<?>query,CriteriaBuilder cb){
                Subquery<LocalDateTime> subquery = query.subquery(LocalDateTime.class);
                Root<Comment> subRoot = subquery.from(Comment.class);
                Expression<LocalDateTime> createDateExpression = subRoot.get("createDate");
                subquery.select(cb.greatest(createDateExpression))
                        .where(cb.equal(subRoot.get("question"),q));
                Expression<LocalDateTime> subqueryExpression = cb.coalesce(subquery,
                       q.get("createDate")
                );
                query.orderBy(cb.desc(subqueryExpression));
            }
            private void orderByCreateDate(Root<Question> q,CriteriaQuery<?>query,CriteriaBuilder cb){
                query.orderBy(cb.desc(q.get("createDate")));
            }
        };
    }
}
