package com.pack.material.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pack.material.common.Result;
import com.pack.material.entity.PackStockRecord;
import com.pack.material.service.PackStockRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/stock-record")
@CrossOrigin
public class PackStockRecordController {

    @Resource
    private PackStockRecordService packStockRecordService;

    @GetMapping("/page")
    public Result<IPage<PackStockRecord>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String materialCode,
                                               @RequestParam(required = false) Integer recordType) {
        log.debug("查询出入库记录: pageNum={}, pageSize={}, materialCode={}, recordType={}",
                pageNum, pageSize, materialCode, recordType);
        return Result.success(packStockRecordService.page(pageNum, pageSize, materialCode, recordType));
    }

    @GetMapping("/{id}")
    public Result<PackStockRecord> getById(@PathVariable Long id) {
        log.debug("查询出入库记录详情: id={}", id);
        return Result.success(packStockRecordService.getById(id));
    }
}
