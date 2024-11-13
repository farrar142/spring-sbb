package com.mysite.sbb.question;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mysite.sbb.user.SiteUser;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Question findBySubject(String subject);
    Question findBySubjectAndContent(String subject, String content);

    List<Question> findBySubjectLike(String subject);
    
    Page<Question> findAll(Pageable pageable);

    Page<Question> findAll(Specification<Question> spec, Pageable pageable);


    Page<Question> findByAuthor(SiteUser author, Pageable pageable);
    
    @Query("SELECT DISTINCT q FROM Question q " +
    "LEFT OUTER JOIN SiteUser u1 ON q.author = u1 "+
    "LEFT OUTER JOIN Answer a ON a.question = q "+
    "LEFT OUTER JOIN SiteUser u2 ON a.author = u2 "+
    "WHERE "+
    "q.subject LIKE %:kw% OR "+
    "q.content LIKE %:kw% OR "+
    "u1.username LIKE %:kw% OR "+
    "a.content LIKE %:kw% OR "+
    "u2.username LIKE %:kw% " +
    "ORDER BY q.createDate DESC")
    Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

    @Query("SELECT q FROM Question q " +
        "LEFT OUTER JOIN SiteUser u1 ON q.author = u1 " +
        "LEFT OUTER JOIN Answer   a  ON a.question = q " +
        "LEFT OUTER JOIN SiteUser u2 ON a.author = u2 " +
        "WHERE " +
        "q.subject LIKE %:kw% OR " +
        "q.content LIKE %:kw% OR " +
        "u1.username LIKE %:kw% OR " +
        "a.content LIKE %:kw% OR " +
        "u2.username LIKE %:kw% " +
        "ORDER BY (SELECT MAX(ans.createDate) FROM Answer ans WHERE ans.question.id = q.id) DESC, q.createDate DESC")
    Page<Question> findQuestionsByKeywordWithLatestAnswerDate(
        @Param("kw") String kw,
        Pageable pageable
    );

    @Query("SELECT q FROM Question q " +
        "LEFT OUTER JOIN SiteUser u1 ON q.author = u1 " +
        "LEFT OUTER JOIN Answer   a  ON a.question = q " +
        "LEFT OUTER JOIN Comment  cm ON cm.question = q " +
        "LEFT OUTER JOIN SiteUser u2 ON a.author = u2 " +
        "LEFT OUTER JOIN SiteUser u3 ON cm.author = u3 " +
        "WHERE " +
        "q.subject LIKE %:kw% OR q.content LIKE %:kw% OR u1.username LIKE %:kw% OR a.content LIKE %:kw% OR u3.username LIKE %:kw% OR cm.content LIKE %:kw% OR u2.username LIKE %:kw% " +
        "ORDER BY (SELECT MAX(cm.createDate) FROM Comment cm WHERE cm.question.id = q.id) DESC, q.createDate DESC")
    Page<Question> findQuestionsByKeywordWithLatestCommentDate(
        @Param("kw") String kw,
        Pageable pageable
    );

    @Query("SELECT DISTINCT q FROM Question q LEFT OUTER JOIN SiteUser u1 ON q.author = u1 LEFT OUTER JOIN Answer a ON a.question = q LEFT OUTER JOIN SiteUser u2 ON a.author = u2 LEFT OUTER JOIN Category c ON q.category = c WHERE (q.subject LIKE %:kw% OR q.content LIKE %:kw% OR u1.username LIKE %:kw% OR a.content LIKE %:kw% OR u2.username LIKE %:kw%) AND c.name=:category ORDER BY q.createDate DESC")
    Page<Question> findAllByKeywordAndCategory(@Param("category") String category, @Param("kw") String kw,
            Pageable pageable);
                    
    @Query("SELECT q FROM Question q " +
        "LEFT OUTER JOIN SiteUser u1 ON q.author = u1 " +
        "LEFT OUTER JOIN Answer   a  ON a.question = q " +
        "LEFT OUTER JOIN SiteUser u2 ON a.author = u2 " +
        "LEFT OUTER JOIN Category c  ON q.category = c " +
        "WHERE c.name = :category " +
        "AND (q.subject LIKE %:kw% OR q.content LIKE %:kw% OR u1.username LIKE %:kw% OR a.content LIKE %:kw% OR u2.username LIKE %:kw%)" +
        "ORDER BY (SELECT MAX(a.createDate) FROM Answer a WHERE a.question.id = q.id) DESC, q.createDate DESC")
    Page<Question> findQuestionsByKeywordAndCategoryWithLatestAnswerDate(
        @Param("category") String category,
        @Param("kw") String kw,
        Pageable pageable
    );

    @Query("SELECT q FROM Question q " +
        "LEFT OUTER JOIN SiteUser u1 ON q.author = u1 " +
        "LEFT OUTER JOIN Answer   a  ON a.question = q " +
        "LEFT OUTER JOIN Comment  cm ON cm.question = q " +
        "LEFT OUTER JOIN SiteUser u2 ON a.author = u2 " +
        "LEFT OUTER JOIN SiteUser u3 ON cm.author = u3 " +
        "LEFT OUTER JOIN Category c  ON q.category = c " +
        "WHERE c.name = :category " +
        "AND (q.subject LIKE %:kw% OR q.content LIKE %:kw% OR u1.username LIKE %:kw% OR a.content LIKE %:kw% OR cm.content LIKE %:kw% OR u2.username LIKE %:kw% OR u3.username LIKE %:kw%) " +
        "ORDER BY (SELECT MAX(cm.createDate) FROM Comment cm WHERE cm.question.id = q.id) DESC, q.createDate DESC")
    Page<Question> findQuestionsByKeywordAndCategoryWithLatestCommentDate(
        @Param("category") String category,
        @Param("kw") String kw,
        Pageable pageable
    );

}