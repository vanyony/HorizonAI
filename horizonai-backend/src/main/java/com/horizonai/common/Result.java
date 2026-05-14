package com.horizonai.common;

/**
 * 统一响应封装
 */
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    // ========== 无参构造器（替代 Lombok @NoArgsConstructor） ==========

    public Result() {}

    // ========== 全参构造器（替代 Lombok @AllArgsConstructor） ==========

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 手动 getter/setter 方法（替代 Lombok @Data） ==========

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    // ========== 静态工厂方法 ==========

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}
