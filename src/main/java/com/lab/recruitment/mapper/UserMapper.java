package com.lab.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.recruitment.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据角色查询用户列表
     */
    List<User> selectUsersByRole(@Param("role") String role);
    
    /**
     * 查询所有管理员（不包括总负责人）
     */
    List<User> selectAllAdmins();
    
    /**
     * 检查用户名是否存在（包含已删除记录）
     */
    int countByUsernameIncludeDeleted(@Param("username") String username, @Param("deleted") Integer deleted);
    
    /**
     * 检查学号是否存在（包含已删除记录）
     */
    int countByStudentIdIncludeDeleted(@Param("studentId") String studentId, @Param("deleted") Integer deleted);
    
    /**
     * 检查邮箱是否存在（包含已删除记录）
     */
    int countByEmailIncludeDeleted(@Param("email") String email, @Param("deleted") Integer deleted);
    
    /**
     * 检查手机号是否存在（包含已删除记录）
     */
    int countByPhoneIncludeDeleted(@Param("phone") String phone, @Param("deleted") Integer deleted);
    
    /**
     * 根据用户名和删除状态查找用户
     */
    User findByUsernameAndDeleted(@Param("username") String username, @Param("deleted") Integer deleted);
    
    /**
     * 恢复已删除的用户记录
     */
    int restoreDeletedUser(@Param("id") Long id, @Param("username") String username, @Param("password") String password, 
                          @Param("realName") String realName, @Param("role") String role, 
                          @Param("studentId") String studentId, @Param("major") String major, 
                          @Param("grade") String grade, @Param("email") String email, 
                          @Param("status") Integer status);
}