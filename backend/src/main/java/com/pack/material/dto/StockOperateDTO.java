package com.pack.material.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class StockOperateDTO implements Serializable {

    @NotNull(message = "物料ID不能为空")
    private Long materialId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @NotBlank(message = "操作人不能为空")
    private String operator;

    private String relatedOrderNo;

    private String remark;
}
