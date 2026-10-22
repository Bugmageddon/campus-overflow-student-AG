package com.campusoverflow.identity.interfaces;

import com.campusoverflow.identity.application.AdminUserService;
import com.campusoverflow.identity.application.CreateStaffCommand;
import com.campusoverflow.identity.application.UserView;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 管理员接口。URL 级授权（hasRole ADMIN）在 SecurityConfig 中配置，领域层仍会再次校验。 */
@Tag(name = "管理 Admin")
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService admin;

    public AdminUserController(AdminUserService admin) {
        this.admin = admin;
    }

    @Operation(summary = "按认证状态列出用户")
    @GetMapping
    public PageResult<UserView> list(@Parameter(hidden = true) Actor actor,
                                     @RequestParam(defaultValue = "false") boolean verified,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return admin.listByVerification(actor, verified, new PageRequest(page, size));
    }

    @Operation(summary = "完成校园身份认证")
    @PostMapping("/{userId}/verify")
    public ResponseEntity<Void> verify(@Parameter(hidden = true) Actor actor, @PathVariable long userId) {
        admin.verify(actor, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "创建教职工账号")
    @PostMapping
    public ResponseEntity<Map<String, Long>> createStaff(@Parameter(hidden = true) Actor actor,
                                                         @Valid @RequestBody CreateStaffRequest req) {
        long id = admin.createStaff(actor, new CreateStaffCommand(req.username(), req.displayName(), req.email(),
                req.password(), req.role(), req.college()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @Operation(summary = "调整全局角色")
    @PutMapping("/{userId}/role")
    public ResponseEntity<Void> changeRole(@Parameter(hidden = true) Actor actor, @PathVariable long userId,
                                           @Valid @RequestBody ChangeRoleRequest req) {
        admin.changeRole(actor, userId, req.role());
        return ResponseEntity.noContent().build();
    }

    public record CreateStaffRequest(@NotBlank String username, @NotBlank String displayName,
                                     @NotBlank String email, @NotBlank String password, @NotNull Role role,
                                     String college) {
    }

    public record ChangeRoleRequest(@NotNull Role role) {
    }
}
