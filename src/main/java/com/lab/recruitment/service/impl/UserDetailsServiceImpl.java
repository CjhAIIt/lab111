package com.lab.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lab.recruitment.entity.User;
import com.lab.recruitment.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从统一用户表查找
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("username", username);
        userQueryWrapper.eq("status", 1);
        userQueryWrapper.eq("deleted", 0);  // 添加检查未删除的账号
        User user = userMapper.selectOne(userQueryWrapper);
        
        if (user != null) {
            // 根据角色分配权限
            String role = user.getRole();
            String authority = "ROLE_" + role.toUpperCase();
            
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(authority))
            );
        }
        
        throw new UsernameNotFoundException("用户不存在: " + username);
    }
}