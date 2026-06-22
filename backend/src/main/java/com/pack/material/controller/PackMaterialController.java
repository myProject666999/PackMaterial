package com.pack.material.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pack.material.common.Result;
import com.pack.material.entity.PackMaterial;
import com.pack.material.service.PackMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/material")
@CrossOrigin
public class PackMaterialController {

    @Resource
    private PackMaterialService packMaterialService;

    @GetMapping("/page")
    public Result<IPage<PackMaterial>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String materialName,
                                            @RequestParam(required = false) String materialType) {
        log.debug("查询物料列表: pageNum={}, pageSize={}, materialName={}, materialType={}",
                pageNum, pageSize, materialName, materialType);
        return Result.success(packMaterialService.page(pageNum, pageSize, materialName, materialType));
    }

    @GetMapping("/list")
    public Result<List<PackMaterial>> list() {
        log.debug("查询所有物料");
        return Result.success(packMaterialService.listAll());
    }

    @GetMapping("/{id}")
    public Result<PackMaterial> getById(@PathVariable Long id) {
        log.debug("查询物料详情: id={}", id);
        return Result.success(packMaterialService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody PackMaterial material) {
        log.debug("新增物料: {}", material);
        return Result.success(packMaterialService.saveMaterial(material));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody PackMaterial material) {
        log.debug("更新物料: {}", material);
        return Result.success(packMaterialService.updateMaterial(material));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        log.debug("删除物料: id={}", id);
        return Result.success(packMaterialService.deleteMaterial(id));
    }
}
