package com.pack.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("purchase_request")
public class PurchaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String requestNo;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String specification;

    private Integer currentStock;

    private Integer safetyStock;

    private Integer suggestedQuantity;

    private String unit;

    private Integer status;

    private LocalDateTime requestTime;

    private String operator;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
