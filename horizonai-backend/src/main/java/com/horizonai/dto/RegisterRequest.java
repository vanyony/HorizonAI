package com.horizonai.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码长度 6-30 个字符")
    private String password;

    private String email;

    private List<Long> interestTagIds;

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<Long> getInterestTagIds() { return interestTagIds; }
    public void setInterestTagIds(List<Long> interestTagIds) { this.interestTagIds = interestTagIds; }
}
