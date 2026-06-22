package com.pack.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pack.material.entity.ProductPackBom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductPackBomMapper extends BaseMapper<ProductPackBom> {

    @Select("SELECT * FROM product_pack_bom WHERE product_id = #{productId} AND is_deleted = 0")
    List<ProductPackBom> selectByProductId(@Param("productId") Long productId);
}
