package com.pack.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("pack_stock_record")
public class PackStockRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String recordNo;

    private Integer recordType;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private Integer quantity;

    private String unit;

    private String operator;

    private LocalDateTime operateTime;

    private String relatedOrderNo;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
