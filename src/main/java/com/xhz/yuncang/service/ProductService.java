package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.Product;

import java.util.*;
import java.util.stream.Collectors;

public interface ProductService extends IService<Product> {

    Product findBySku(String sku);

    Product findByName(String name);

    Product findById(String id);

    Boolean addOneProduct(Product product);

    Boolean deleteBySku(String sku);

    List<Product> findAll();

    Boolean deleteAll();

    Boolean updateBySku(Product product);

    Boolean updateProductInfoBySku(String sku, String name, String description,
                                   Double weight, Double length, Double width, Double height);

    List<Product> findProductsBySkus(List<String> skus);


    // 根据名称列表获取商品 Map<Name, Sku>
    default Map<String, String> getNameToSkuMap(Collection<String> names) {
        if (names == null || names.isEmpty()){
            return Collections.emptyMap();
        }
        // 使用Set去重
        Set<String> nameSet = new HashSet<>(names);
        List<Product> products = this.list(Wrappers.<Product>lambdaQuery()
                .in(Product::getName, nameSet)
                .select(Product::getName, Product::getSku));
        // (oldValue, newValue) -> oldValue 防止名称重复时报错
        return products.stream()
                .collect(Collectors.toMap(Product::getName, Product::getSku, (oldValue, newValue) -> oldValue));
    }
}