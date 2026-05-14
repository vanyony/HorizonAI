package com.horizonai.vo;

public class LoginVO {

    private String token;
    private String username;
    private String role;

    // ========== 手动全参构造器（替代 Lombok @AllArgsConstructor） ==========

    public LoginVO(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
