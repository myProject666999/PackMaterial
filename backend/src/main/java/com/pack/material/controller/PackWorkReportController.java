package com.pack.material.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pack.material.common.Result;
import com.pack.material.dto.WorkReportDTO;
import com.pack.material.entity.PackWorkReport;
import com.pack.material.entity.WorkReportMaterial;
import com.pack.material.service.PackWorkReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/work-report")
@CrossOrigin
public class PackWorkReportController {

    @Resource
    private PackWorkReportService packWorkReportService;

    @GetMapping("/page")
    public Result<IPage<PackWorkReport>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Integer status) {
        log.debug("查询报工列表: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        return Result.success(packWorkReportService.page(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        log.debug("查询报工详情: id={}", id);
        PackWorkReport report = packWorkReportService.getById(id);
        List<WorkReportMaterial> materials = packWorkReportService.getReportMaterials(id);
        Map<String, Object> result = new HashMap<>();
        result.put("report", report);
        result.put("materials", materials);
        return Result.success(result);
    }

    @PostMapping
    public Result<PackWorkReport> create(@Validated @RequestBody WorkReportDTO dto) {
        log.debug("创建报工单: {}", dto);
        return Result.success(packWorkReportService.createReport(dto));
    }

    @PostMapping("/deduct/{id}")
    public Result<Boolean> deductStock(@PathVariable Long id) {
        log.debug("报工核减库存: id={}", id);
        return Result.success(packWorkReportService.deductStock(id));
    }

    @GetMapping("/materials/{reportId}")
    public Result<List<WorkReportMaterial>> getMaterials(@PathVariable Long reportId) {
        log.debug("查询报工用料明细: reportId={}", reportId);
        return Result.success(packWorkReportService.getReportMaterials(reportId));
    }
}
