package com.campusoverflow.bootstrap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI 3 文档（ADR-007）：前端通过 npm run gen:api 从 /v3/api-docs 生成类型。 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusOverflowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CampusOverflow API")
                .version("v1")
                .description("大学校园版 Stack Overflow。认证方式：会话 Cookie（SESSION）+ CSRF 双提交 Cookie（XSRF-TOKEN / X-XSRF-TOKEN）。"
                        + "错误响应遵循 RFC 9457 Problem Details，扩展字段 code 为业务错误码。")
                .license(new License().name("仅供教学使用")));
    }
}
