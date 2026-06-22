package com.pack.material.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pack.material.common.Result;
import com.pack.material.entity.FinishedProduct;
import com.pack.material.service.FinishedProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product")
@CrossOrigin
public class FinishedProductController {

    @Resource
    private FinishedProductService finishedProductService;

    @GetMapping("/page")
    public Result<IPage<FinishedProduct>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String productName,
                                               @RequestParam(required = false) String category) {
        log.debug("查询成品列表: pageNum={}, pageSize={}, productName={}, category={}",
                pageNum, pageSize, productName, category);
        return Result.success(finishedProductService.page(pageNum, pageSize, productName, category));
    }

    @GetMapping("/list")
    public Result<List<FinishedProduct>> list() {
        log.debug("查询所有成品");
        return Result.success(finishedProductService.listAll());
    }

    @GetMapping("/{id}")
    public Result<FinishedProduct> getById(@PathVariable Long id) {
        log.debug("查询成品详情: id={}", id);
        return Result.success(finishedProductService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody FinishedProduct product) {
        log.debug("新增成品: {}", product);
        return Result.success(finishedProductService.save(product));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody FinishedProduct product) {
        log.debug("更新成品: {}", product);
        return Result.success(finishedProductService.updateById(product));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        log.debug("删除成品: id={}", id);
        return Result.success(finishedProductService.removeById(id));
    }
}
