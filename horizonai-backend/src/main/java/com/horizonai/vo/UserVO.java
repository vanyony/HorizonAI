package com.horizonai.vo;

public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatar;

    // ========== 手动全参构造器（替代 Lombok @AllArgsConstructor） ==========

    public UserVO(Long id, String username, String email, String role, String avatar) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
    }

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
