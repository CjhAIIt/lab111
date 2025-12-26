package com.lab.recruitment.controller;

import com.lab.recruitment.dto.LoginDTO;
import com.lab.recruitment.dto.UserRegisterDTO;
import com.lab.recruitment.service.AdminService;
import com.lab.recruitment.service.UserService;
import com.lab.recruitment.utils.Result;
import com.lab.recruitment.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 负责处理用户注册、登录等认证相关的请求
 * 支持用户和管理员两种角色的认证操作
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    /**
     * 用户注册接口
     * 处理新用户的注册请求，支持对已删除用户的重新注册
     * @param registerDTO 用户注册信息，包含用户名、密码、邮箱、手机号等必填信息
     * @return 注册结果，成功返回成功消息，失败返回具体错误原因
     */
    @PostMapping("/register")
    public Result<Object> register(@Validated @RequestBody UserRegisterDTO registerDTO) {
        try {
            boolean success = userService.register(registerDTO);
            if (success) {
                return Result.success("注册成功");
            } else {
                return Result.error("注册失败");
            }
        } catch (Exception e) {
            // 处理数据库约束违反异常
            if (e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException) {
                String message = e.getCause().getMessage();
                
                // 检查是否是用户名重复
                if (message.contains("Duplicate entry") && message.contains("username")) {
                    // 检查是否是已删除的记录
                    if (userService.existsByUsernameAndDeleted(registerDTO.getUsername(), true)) {
                        // 如果是已删除的记录，允许注册
                        boolean success = userService.registerWithDeletedCheck(registerDTO);
                        if (success) {
                            return Result.success("注册成功");
                        } else {
                            return Result.error("注册失败");
                        }
                    }
                    return Result.error("该学号已被注册，请使用其他学号");
                }
                // 检查是否是邮箱重复
                else if (message.contains("Duplicate entry") && message.contains("email")) {
                    // 检查是否是已删除的记录
                    if (userService.existsByEmailAndDeleted(registerDTO.getEmail(), true)) {
                        // 如果是已删除的记录，允许注册
                        boolean success = userService.registerWithDeletedCheck(registerDTO);
                        if (success) {
                            return Result.success("注册成功");
                        } else {
                            return Result.error("注册失败");
                        }
                    }
                    return Result.error("该邮箱已被注册，请使用其他邮箱");
                }
                // 检查是否是手机号重复
                else if (message.contains("Duplicate entry") && message.contains("phone")) {
                    // 检查是否是已删除的记录
                    if (registerDTO.getPhone() != null && userService.existsByPhoneAndDeleted(registerDTO.getPhone(), true)) {
                        // 如果是已删除的记录，允许注册
                        boolean success = userService.registerWithDeletedCheck(registerDTO);
                        if (success) {
                            return Result.success("注册成功");
                        } else {
                            return Result.error("注册失败");
                        }
                    }
                    return Result.error("该手机号已被注册，请使用其他手机号");
                }
                return Result.error("数据冲突，请检查输入信息");
            }
            // 处理全局异常处理器返回的错误代码
            else if (e.getMessage() != null) {
                if ("DB_DUPLICATE_USERNAME".equals(e.getMessage())) {
                    // 检查是否是已删除的记录
                    if (userService.existsByUsernameAndDeleted(registerDTO.getUsername(), true)) {
                        // 如果是已删除的记录，允许注册
                        boolean success = userService.registerWithDeletedCheck(registerDTO);
                        if (success) {
                            return Result.success("注册成功");
                        } else {
                            return Result.error("注册失败");
                        }
                    }
                    return Result.error("该学号已被注册，请使用其他学号");
                } else if ("DB_DUPLICATE_EMAIL".equals(e.getMessage())) {
                    // 检查是否是已删除的记录
                    if (userService.existsByEmailAndDeleted(registerDTO.getEmail(), true)) {
                        // 如果是已删除的记录，允许注册
                        boolean success = userService.registerWithDeletedCheck(registerDTO);
                        if (success) {
                            return Result.success("注册成功");
                        } else {
                            return Result.error("注册失败");
                        }
                    }
                    return Result.error("该邮箱已被注册，请使用其他邮箱");
                } else if ("DB_DUPLICATE_PHONE".equals(e.getMessage())) {
                    // 检查是否是已删除的记录
                    if (registerDTO.getPhone() != null && userService.existsByPhoneAndDeleted(registerDTO.getPhone(), true)) {
                        // 如果是已删除的记录，允许注册
                        boolean success = userService.registerWithDeletedCheck(registerDTO);
                        if (success) {
                            return Result.success("注册成功");
                        } else {
                            return Result.error("注册失败");
                        }
                    }
                    return Result.error("该手机号已被注册，请使用其他手机号");
                }
            }
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登录接口
     * 支持用户和管理员两种身份登录
     * 先尝试以用户身份登录，失败后尝试以管理员身份登录
     * @param loginDTO 登录信息，包含用户名和密码
     * @return 登录结果，成功返回包含token和用户信息的LoginVO对象
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        try {
            // 先尝试用户登录
            try {
                LoginVO loginVO = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
                return Result.success(loginVO);
            } catch (Exception e) {
                // 用户登录失败，尝试管理员登录
                LoginVO loginVO = adminService.login(loginDTO.getUsername(), loginDTO.getPassword());
                return Result.success(loginVO);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}