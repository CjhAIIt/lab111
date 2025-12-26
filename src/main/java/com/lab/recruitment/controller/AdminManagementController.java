package com.lab.recruitment.controller;

import com.lab.recruitment.entity.User;
import com.lab.recruitment.service.AdminManagementService;
import com.lab.recruitment.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员管理控制器
 */
@RestController
@RequestMapping("/admin-management")
public class AdminManagementController {
    
    @Autowired
    private AdminManagementService adminManagementService;
    
    /**
     * 为实验室指定管理员
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Object> assignAdminToLab(@RequestBody Map<String, Long> request) {
        Long labId = request.get("labId");
        Long userId = request.get("userId");
        
        if (labId == null || userId == null) {
            return Result.error("实验室ID和用户ID不能为空");
        }
        
        return adminManagementService.assignAdminToLab(labId, userId);
    }
    
    /**
     * 移除实验室管理员
     */
    @DeleteMapping("/remove/{labId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Object> removeAdminFromLab(@PathVariable Long labId) {
        return adminManagementService.removeAdminFromLab(labId);
    }
    
    /**
     * 获取实验室管理员信息
     */
    @GetMapping("/lab/{labId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<User> getLabAdmin(@PathVariable Long labId) {
        return adminManagementService.getLabAdmin(labId);
    }
    
    /**
     * 获取所有管理员及其关联的实验室
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<User>> getAllAdminsWithLabs() {
        return adminManagementService.getAllAdminsWithLabs();
    }
    
    /**
     * 检查用户是否可以设置为管理员
     */
    @GetMapping("/can-be-admin/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Boolean> canUserBeAdmin(@PathVariable Long userId) {
        return adminManagementService.canUserBeAdmin(userId);
    }
}