package com.pack.material.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pack.material.common.Result;
import com.pack.material.dto.StockOperateDTO;
import com.pack.material.entity.PackInventory;
import com.pack.material.service.PackInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/inventory")
@CrossOrigin
public class PackInventoryController {

    @Resource
    private PackInventoryService packInventoryService;

    @GetMapping("/page")
    public Result<IPage<PackInventory>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String materialCode,
                                             @RequestParam(required = false) Boolean lowStock) {
        log.debug("查询库存列表: pageNum={}, pageSize={}, materialCode={}, lowStock={}",
                pageNum, pageSize, materialCode, lowStock);
        return Result.success(packInventoryService.page(pageNum, pageSize, materialCode, lowStock));
    }

    @GetMapping("/{materialId}")
    public Result<PackInventory> getByMaterialId(@PathVariable Long materialId) {
        log.debug("查询物料库存: materialId={}", materialId);
        return Result.success(packInventoryService.getByMaterialId(materialId));
    }

    @GetMapping("/available/{materialId}")
    public Result<Map<String, Object>> getAvailableStock(@PathVariable Long materialId) {
        log.debug("查询物料可用库存: materialId={}", materialId);
        Integer stock = packInventoryService.getAvailableStock(materialId);
        Map<String, Object> result = new HashMap<>();
        result.put("materialId", materialId);
        result.put("availableStock", stock);
        return Result.success(result);
    }

    @PostMapping("/in")
    public Result<Boolean> stockIn(@Validated @RequestBody StockOperateDTO dto) {
        log.debug("采购入库: {}", dto);
        return Result.success(packInventoryService.stockIn(dto));
    }

    @PostMapping("/out")
    public Result<Boolean> stockOut(@Validated @RequestBody StockOperateDTO dto) {
        log.debug("领用出库: {}", dto);
        return Result.success(packInventoryService.stockOut(dto));
    }
}
