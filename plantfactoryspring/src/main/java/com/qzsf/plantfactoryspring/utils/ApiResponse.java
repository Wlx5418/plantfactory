package com.qzsf.plantfactoryspring.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应格式
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间
     */
    private LocalDateTime timestamp;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功响应（带自定义消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code(500)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失败响应（带自定义状态码）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 参数错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .code(400)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .code(401)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 禁止访问响应
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .code(403)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 资源未找到响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .code(404)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 服务器内部错误响应
     */
    public static <T> ApiResponse<T> internalServerError(String message) {
        return ApiResponse.<T>builder()
                .code(500)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}