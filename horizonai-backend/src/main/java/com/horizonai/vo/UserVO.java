package com.horizonai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatar;
}
