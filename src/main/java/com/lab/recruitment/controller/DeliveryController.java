package com.lab.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.recruitment.dto.DeliveryDTO;
import com.lab.recruitment.service.DeliveryService;
import com.lab.recruitment.utils.JwtUtils;
import com.lab.recruitment.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/deliver")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Object> deliver(@Validated @RequestBody DeliveryDTO deliveryDTO) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean success = deliveryService.deliver(deliveryDTO, username);
            if (success) {
                return Result.success("投递成功");
            } else {
                return Result.error("投递失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Page<Map<String, Object>>> getDeliveryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long labId,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Integer auditStatus) {
        try {
            Page<Map<String, Object>> deliveryPage = deliveryService.getDeliveryPage(
                pageNum, pageSize, labId, realName, studentId, auditStatus);
            return Result.success(deliveryPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Page<Map<String, Object>>> getMyDeliveryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String labName,
            @RequestParam(required = false) Integer auditStatus) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Page<Map<String, Object>> deliveryPage = deliveryService.getMyDeliveryPage(
                pageNum, pageSize, username, labName, auditStatus);
            return Result.success(deliveryPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/audit/{deliveryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Object> audit(@PathVariable Long deliveryId,
                               @RequestParam Integer auditStatus,
                               @RequestParam(required = false) String auditRemark) {
        try {
            boolean success = deliveryService.audit(deliveryId, auditStatus, auditRemark);
            if (success) {
                return Result.success("审核成功");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/admit/{deliveryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Object> admit(@PathVariable Long deliveryId) {
        try {
            boolean success = deliveryService.admit(deliveryId);
            if (success) {
                return Result.success("录取成功");
            } else {
                return Result.error("录取失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics/{labId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public Result<Map<String, Object>> getStatistics(@PathVariable Long labId) {
        try {
            Map<String, Object> statistics = deliveryService.getStatistics(labId);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}