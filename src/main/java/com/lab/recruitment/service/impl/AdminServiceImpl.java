package com.lab.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.recruitment.entity.User;
import com.lab.recruitment.mapper.AdminMapper;
import com.lab.recruitment.service.AdminService;
import com.lab.recruitment.utils.JwtUtils;
import com.lab.recruitment.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, User> implements AdminService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public LoginVO login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("status", 1);
        queryWrapper.eq("deleted", 0);  // 添加检查未删除的账号
        queryWrapper.in("role", "admin", "super_admin");
        User admin = this.getOne(queryWrapper);
        
        if (admin == null) {
            throw new RuntimeException("管理员账号不存在或已被禁用");
        }

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT令牌
        String token = jwtUtils.generateToken(username, admin.getRole());

        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUsername(admin.getUsername());
        loginVO.setRealName(admin.getRealName());
        loginVO.setRole(admin.getRole());

        return loginVO;
    }
}