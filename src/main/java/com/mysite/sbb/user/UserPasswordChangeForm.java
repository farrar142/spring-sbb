package com.mysite.sbb.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordChangeForm {
    @NotEmpty(message = "기존 비밀번호는 필수항목입니다.")
    private String originPassword;

    @NotEmpty(message = "새 비밀번호는 필수항목입니다.")
    @Min(value=8)
    private String password1;

    @NotEmpty(message = "새 비밀번호 확인은 필수항목입니다.")
    @Min(value=8)
    private String password2;
}
