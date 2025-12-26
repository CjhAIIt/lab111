package com.lab.recruitment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_lab")
public class Lab {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("lab_name")
    private String labName;
    
    @TableField("lab_desc")
    private String labDesc;
    
    @TableField("require_skill")
    private String requireSkill;
    
    @TableField("recruit_num")
    private Integer recruitNum;
    
    @TableField("current_num")
    private Integer currentNum;
    
    @TableField("status")
    private Integer status;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}