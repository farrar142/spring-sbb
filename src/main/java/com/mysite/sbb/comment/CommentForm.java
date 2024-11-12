package com.mysite.sbb.comment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentForm {
    @NotEmpty(message="내용은 필수입니다")
    @Size(max=200)
    private String content;

    private Integer answerId;
    private Integer questionId;
}
