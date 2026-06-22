package com.pack.material.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class WorkReportDTO implements Serializable {

    @NotNull(message = "成品ID不能为空")
    private Long productId;

    @NotNull(message = "生产数量不能为空")
    private Integer productionQuantity;

    @NotBlank(message = "操作人不能为空")
    private String operator;

    private String workLine;

    private String remark;
}
