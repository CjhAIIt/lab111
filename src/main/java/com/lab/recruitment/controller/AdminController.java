package com.lab.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.recruitment.entity.User;
import com.lab.recruitment.service.AdminService;
import com.lab.recruitment.service.UserService;
import com.lab.recruitment.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AdminService adminService;

    /**
     * 获取学生列表（管理员可访问）
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String major) {
        try {
            Page<User> userPage = userService.getUserPage(pageNum, pageSize, realName, studentId, major, "student");
            return Result.success(userPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取管理员列表（仅总负责人可访问）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Page<User>> getAdminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String username) {
        try {
            Page<User> adminPage = userService.getUserPage(pageNum, pageSize, realName, null, null, "ADMIN");
            return Result.success(adminPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户（管理员可访问）
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result deleteUser(@PathVariable Long id) {
        try {
            boolean success = userService.removeById(id);
            if (success) {
                return Result.success("用户删除成功");
            } else {
                return Result.error("用户删除失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}