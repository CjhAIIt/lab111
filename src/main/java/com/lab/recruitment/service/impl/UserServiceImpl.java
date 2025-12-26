package com.lab.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.recruitment.dto.UserRegisterDTO;
import com.lab.recruitment.entity.Delivery;
import com.lab.recruitment.entity.User;
import com.lab.recruitment.mapper.DeliveryMapper;
import com.lab.recruitment.mapper.UserMapper;
import com.lab.recruitment.service.UserService;
import com.lab.recruitment.utils.JwtUtils;
import com.lab.recruitment.vo.LoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private DeliveryMapper deliveryMapper;

    /**
     * 用户注册方法
     * 处理新用户注册，支持对已删除用户的重新注册
     * 检查用户名、学号、手机号、邮箱的唯一性
     * @param registerDTO 用户注册信息，包含用户名、密码、邮箱、手机号等必填信息
     * @return 注册是否成功
     * @throws RuntimeException 当用户名、学号、手机号或邮箱已存在时抛出异常
     */
    @Override
    public boolean register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在（使用自定义SQL方法避免@TableLogic冲突）
        if (baseMapper.countByUsernameIncludeDeleted(registerDTO.getUsername(), 0) > 0) {
            throw new RuntimeException("该学号已被注册，请使用其他学号");
        }
        
        // 检查是否有已删除的同名记录
        if (baseMapper.countByUsernameIncludeDeleted(registerDTO.getUsername(), 1) > 0) {
            // 如果有已删除的同名记录，使用特殊注册方法
            return registerWithDeletedCheck(registerDTO);
        }

        // 检查学号是否已存在（如果学号与用户名不同）
        if (registerDTO.getStudentId() != null && !registerDTO.getStudentId().equals(registerDTO.getUsername())) {
            if (baseMapper.countByStudentIdIncludeDeleted(registerDTO.getStudentId(), 0) > 0) {
                throw new RuntimeException("学号已存在");
            }
            
            // 检查是否有已删除的同同学号记录
            if (baseMapper.countByStudentIdIncludeDeleted(registerDTO.getStudentId(), 1) > 0) {
                // 如果有已删除的同同学号记录，使用特殊注册方法
                return registerWithDeletedCheck(registerDTO);
            }
        }

        // 检查手机号是否已存在（如果提供了手机号）
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().isEmpty()) {
            if (baseMapper.countByPhoneIncludeDeleted(registerDTO.getPhone(), 0) > 0) {
                throw new RuntimeException("手机号已存在");
            }
            
            // 检查是否有已删除的同手机号记录
            if (baseMapper.countByPhoneIncludeDeleted(registerDTO.getPhone(), 1) > 0) {
                // 如果有已删除的同手机号记录，使用特殊注册方法
                return registerWithDeletedCheck(registerDTO);
            }
        }

        // 检查邮箱是否已存在
        if (baseMapper.countByEmailIncludeDeleted(registerDTO.getEmail(), 0) > 0) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 检查是否有已删除的同邮箱记录
        if (baseMapper.countByEmailIncludeDeleted(registerDTO.getEmail(), 1) > 0) {
            // 如果有已删除的同邮箱记录，使用特殊注册方法
            return registerWithDeletedCheck(registerDTO);
        }

        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole("student"); // 注册用户默认为学生角色
        user.setGrade("大一"); // 默认年级为大一

        return this.save(user);
    }

    @Override
    public LoginVO login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("status", 1);
        User user = this.getOne(queryWrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在或已被禁用");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT令牌
        String token = jwtUtils.generateToken(username, user.getRole());

        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(user.getRole());

        return loginVO;
    }
    
    @Override
    public User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.getOne(queryWrapper);
    }
    
    @Override
    public List<User> getUsersByRole(String role) {
        return baseMapper.selectUsersByRole(role);
    }
    
    @Override
    public Page<User> getUserPage(Integer pageNum, Integer pageSize, String realName, String studentId, String major, String role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        if (realName != null && !realName.isEmpty()) {
            queryWrapper.like("real_name", realName);
        }
        
        if (studentId != null && !studentId.isEmpty()) {
            queryWrapper.like("student_id", studentId);
        }
        
        if (major != null && !major.isEmpty()) {
            queryWrapper.like("major", major);
        }
        
        if (role != null && !role.isEmpty()) {
            if ("ADMIN".equals(role)) {
                queryWrapper.in("role", "admin", "super_admin");
            } else {
                queryWrapper.eq("role", role.toLowerCase());
            }
        }
        
        queryWrapper.orderByDesc("create_time");
        
        return this.page(page, queryWrapper);
    }
    
    @Override
    public List<User> selectAllAdmins() {
        return baseMapper.selectAllAdmins();
    }
    
    @Override
    public User selectAdminById(Long id) {
        User admin = this.getById(id);
        if (admin != null && !"admin".equals(admin.getRole())) {
            throw new RuntimeException("该用户不是管理员");
        }
        return admin;
    }
    
    @Override
    public boolean addAdmin(User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 设置管理员角色和默认值
        user.setRole("admin");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCanEdit(1);
        
        return this.save(user);
    }
    
    @Override
    public boolean updateAdmin(User user) {
        // 检查管理员是否存在
        User existingAdmin = this.getById(user.getId());
        if (existingAdmin == null) {
            throw new RuntimeException("管理员不存在");
        }
        
        if (!"admin".equals(existingAdmin.getRole())) {
            throw new RuntimeException("该用户不是管理员");
        }
        
        // 不允许修改角色和密码（除非有特殊需求）
        user.setRole(existingAdmin.getRole());
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingAdmin.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        return this.updateById(user);
    }
    
    @Override
    @Transactional
    public boolean deleteAdmin(Long id) {
        User admin = this.getById(id);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        
        if (!"admin".equals(admin.getRole())) {
            throw new RuntimeException("只能删除管理员账号");
        }
        
        // 检查是否有学生投递到该管理员负责的实验室
        if (admin.getLabId() != null) {
            QueryWrapper<Delivery> deliveryQuery = new QueryWrapper<>();
            deliveryQuery.eq("lab_id", admin.getLabId());
            deliveryQuery.eq("deleted", 0);
            Long deliveryCount = deliveryMapper.selectCount(deliveryQuery);
            
            if (deliveryCount > 0) {
                throw new RuntimeException("该管理员负责的实验室已有学生投递，无法删除。请先处理所有投递记录。");
            }
        }
        
        return this.removeById(id);
    }
    
    @Override
    public boolean resetAdminPassword(Long adminId, String newPassword) {
        User admin = this.getById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }
        
        if (!"admin".equals(admin.getRole())) {
            throw new RuntimeException("只能重置管理员密码");
        }
        
        admin.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(admin);
    }
    
    @Override
    public boolean existsByUsernameAndDeleted(String username, boolean deleted) {
        return baseMapper.countByUsernameIncludeDeleted(username, deleted ? 1 : 0) > 0;
    }
    
    @Override
    public boolean existsByEmailAndDeleted(String email, boolean deleted) {
        return baseMapper.countByEmailIncludeDeleted(email, deleted ? 1 : 0) > 0;
    }
    
    @Override
    public boolean existsByPhoneAndDeleted(String phone, boolean deleted) {
        return baseMapper.countByPhoneIncludeDeleted(phone, deleted ? 1 : 0) > 0;
    }
    
    @Override
    @Transactional
    public boolean registerWithDeletedCheck(UserRegisterDTO registerDTO) {
        // 查找已删除的记录
        User deletedUser = baseMapper.findByUsernameAndDeleted(registerDTO.getUsername(), 1);
        
        if (deletedUser != null) {
            // 恢复已删除的记录
            String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
            int result = baseMapper.restoreDeletedUser(
                deletedUser.getId(), 
                registerDTO.getUsername(), 
                encodedPassword,
                registerDTO.getRealName(), 
                deletedUser.getRole(), 
                registerDTO.getStudentId(), 
                registerDTO.getMajor(), 
                registerDTO.getGrade(), 
                registerDTO.getEmail(), 
                1
            );
            return result > 0;
        } else {
            // 如果没有找到已删除的记录，执行正常注册
            return this.register(registerDTO);
        }
    }
}