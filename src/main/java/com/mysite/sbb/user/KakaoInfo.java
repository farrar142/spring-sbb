package com.mysite.sbb.user;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoInfo {
    private Long id;
    private String nickname;

    public KakaoInfo(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }
}