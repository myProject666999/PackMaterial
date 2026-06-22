package com.pack.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("work_report_material")
public class WorkReportMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reportId;

    private String reportNo;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private BigDecimal standardQuantity;

    private BigDecimal actualQuantity;

    private BigDecimal differenceQuantity;

    private String unit;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
