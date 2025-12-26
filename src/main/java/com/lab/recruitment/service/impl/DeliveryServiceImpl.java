package com.lab.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.recruitment.dto.DeliveryDTO;
import com.lab.recruitment.entity.Delivery;
import com.lab.recruitment.entity.Lab;
import com.lab.recruitment.entity.User;
import com.lab.recruitment.mapper.DeliveryMapper;
import com.lab.recruitment.service.DeliveryService;
import com.lab.recruitment.service.LabService;
import com.lab.recruitment.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeliveryServiceImpl extends ServiceImpl<DeliveryMapper, Delivery> implements DeliveryService {

    @Autowired
    private UserService userService;

    @Autowired
    private LabService labService;

    @Override
    @Transactional
    public boolean deliver(DeliveryDTO deliveryDTO, String username) {
        // 获取用户信息
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("username", username);
        User user = userService.getOne(userQuery);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 获取实验室信息
        Lab lab = labService.getById(deliveryDTO.getLabId());
        if (lab == null) {
            throw new RuntimeException("实验室不存在");
        }

        // 检查实验室是否在招新中
        if (lab.getStatus() != 1) {
            throw new RuntimeException("实验室当前不在招新中");
        }

        // 检查是否已经投递过该实验室
        QueryWrapper<Delivery> deliveryQuery = new QueryWrapper<>();
        deliveryQuery.eq("user_id", user.getId());
        deliveryQuery.eq("lab_id", deliveryDTO.getLabId());
        if (this.count(deliveryQuery) > 0) {
            throw new RuntimeException("您已投递过该实验室，不能重复投递");
        }

        // 创建投递记录
        Delivery delivery = new Delivery();
        BeanUtils.copyProperties(deliveryDTO, delivery);
        delivery.setUserId(user.getId());
        delivery.setAuditStatus(0); // 待审核
        delivery.setIsAdmitted(0); // 未录取

        boolean result = this.save(delivery);

        // 更新实验室已投递人数
        if (result) {
            lab.setCurrentNum(lab.getCurrentNum() + 1);
            labService.updateById(lab);
        }

        return result;
    }

    @Override
    public Page<Map<String, Object>> getDeliveryPage(Integer pageNum, Integer pageSize, Long labId, 
                                                    String realName, String studentId, Integer auditStatus) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectDeliveryPage(page, labId, realName, studentId, auditStatus);
    }

    @Override
    public Page<Map<String, Object>> getMyDeliveryPage(Integer pageNum, Integer pageSize, String username, 
                                                      String labName, Integer auditStatus) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectMyDeliveryPage(page, username, labName, auditStatus);
    }

    @Override
    @Transactional
    public boolean audit(Long deliveryId, Integer auditStatus, String auditRemark) {
        Delivery delivery = this.getById(deliveryId);
        if (delivery == null) {
            throw new RuntimeException("投递记录不存在");
        }

        delivery.setAuditStatus(auditStatus);
        delivery.setAuditRemark(auditRemark);
        delivery.setAuditTime(LocalDateTime.now());

        return this.updateById(delivery);
    }

    @Override
    @Transactional
    public boolean admit(Long deliveryId) {
        Delivery delivery = this.getById(deliveryId);
        if (delivery == null) {
            throw new RuntimeException("投递记录不存在");
        }

        if (delivery.getAuditStatus() != 1) {
            throw new RuntimeException("只能录取审核通过的投递记录");
        }

        // 获取实验室信息，检查录取人数是否已满
        Lab lab = labService.getById(delivery.getLabId());
        if (lab == null) {
            throw new RuntimeException("实验室不存在");
        }

        Integer admittedCount = baseMapper.countAdmittedByLabId(delivery.getLabId());
        if (admittedCount >= lab.getRecruitNum()) {
            throw new RuntimeException("实验室录取人数已满");
        }

        delivery.setIsAdmitted(1);
        delivery.setAdmitTime(LocalDateTime.now());

        return this.updateById(delivery);
    }

    @Override
    public Map<String, Object> getStatistics(Long labId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总投递人数
        QueryWrapper<Delivery> totalQuery = new QueryWrapper<>();
        totalQuery.eq("lab_id", labId);
        statistics.put("totalCount", this.count(totalQuery));
        
        // 待审核人数
        Integer pendingCount = baseMapper.countByLabIdAndAuditStatus(labId, 0);
        statistics.put("pendingCount", pendingCount);
        
        // 审核通过人数
        Integer approvedCount = baseMapper.countByLabIdAndAuditStatus(labId, 1);
        statistics.put("approvedCount", approvedCount);
        
        // 审核拒绝人数
        Integer rejectedCount = baseMapper.countByLabIdAndAuditStatus(labId, 2);
        statistics.put("rejectedCount", rejectedCount);
        
        // 已录取人数
        Integer admittedCount = baseMapper.countAdmittedByLabId(labId);
        statistics.put("admittedCount", admittedCount);
        
        return statistics;
    }
}