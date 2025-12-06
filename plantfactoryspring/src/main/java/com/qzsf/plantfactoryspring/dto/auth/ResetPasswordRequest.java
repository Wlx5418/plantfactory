package com.qzsf.plantfactoryspring.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求DTO
 *
 * @author Plant Factory Team
 * @since 3.0.0
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "重置令牌不能为空")
    private String token;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}