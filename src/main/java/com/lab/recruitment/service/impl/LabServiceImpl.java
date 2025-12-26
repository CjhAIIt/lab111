package com.lab.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.recruitment.dto.LabWithAdminDTO;
import com.lab.recruitment.entity.Lab;
import com.lab.recruitment.entity.User;
import com.lab.recruitment.mapper.LabMapper;
import com.lab.recruitment.mapper.UserMapper;
import com.lab.recruitment.service.LabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LabServiceImpl extends ServiceImpl<LabMapper, Lab> implements LabService {
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Lab> getLabPage(Integer pageNum, Integer pageSize, String labName, Integer status) {
        Page<Lab> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectLabPage(page, labName, status);
    }

    @Override
    public Lab getLabById(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean createLab(Lab lab) {
        return this.save(lab);
    }

    @Override
    public boolean updateLab(Lab lab) {
        return this.updateById(lab);
    }

    @Override
    public boolean deleteLab(Long id) {
        return this.removeById(id);
    }
    
    @Override
    public List<LabWithAdminDTO> getLabsWithAdmin() {
        List<Lab> labs = this.list();
        List<LabWithAdminDTO> result = new ArrayList<>();
        
        for (Lab lab : labs) {
            LabWithAdminDTO dto = new LabWithAdminDTO();
            dto.setLab(lab);
            
            // 查找该实验室的管理员
            QueryWrapper<User> adminQuery = new QueryWrapper<>();
            adminQuery.eq("role", "admin")
                     .eq("lab_id", lab.getId())
                     .eq("deleted", 0);
            User admin = userMapper.selectOne(adminQuery);
            
            dto.setAdmin(admin);
            dto.setCreateTime(lab.getCreateTime());
            dto.setUpdateTime(lab.getUpdateTime());
            
            result.add(dto);
        }
        
        return result;
    }
}