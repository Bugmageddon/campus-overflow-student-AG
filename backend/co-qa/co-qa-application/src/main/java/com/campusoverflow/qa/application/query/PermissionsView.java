package com.campusoverflow.qa.application.query;

/** 当前用户对问题可执行的操作（前端据此显示按钮；后端仍会在领域层校验）。 */
public record PermissionsView(boolean canEdit, boolean canDelete, boolean canAccept, boolean canManage) {
    public static final PermissionsView NONE = new PermissionsView(false, false, false, false);
}
