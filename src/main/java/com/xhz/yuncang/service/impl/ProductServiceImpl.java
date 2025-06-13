package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.Product;
import com.xhz.yuncang.entity.User;
import com.xhz.yuncang.mapper.ProductMapper;
import com.xhz.yuncang.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Product findBySku(String sku) {
        return lambdaQuery()
                .eq(Product::getSku, sku)
                .one();
    }

    @Override
    public Product findByName(String name) {
        return lambdaQuery()
                .eq(Product::getName,name)
                .one();
    }

    public Product findById(String id){
        return lambdaQuery()
                .eq(Product::getId,id)
                .one();
    }

    @Override
    public Boolean addOneProduct(Product product) {
        return save(product);
    }

    @Override
    public Boolean deleteBySku(String sku) {
        return lambdaUpdate()
                .eq(Product::getSku, sku)
                .remove();
    }

    @Override
    public List<Product> findAll() {
        return list();
    }

    @Override
    public Boolean deleteAll() {
        return remove(null);
    }

    @Override
    public Boolean updateBySku(Product product) {
        return lambdaUpdate()
                .eq(Product::getSku, product.getSku())
                .set(Product::getName, product.getName())
                .set(Product::getDescription, product.getDescription())
                .set(Product::getWeight, product.getWeight())
                .set(Product::getLength, product.getLength())
                .set(Product::getWidth, product.getWidth())
                .set(Product::getHeight, product.getHeight())
                .update();
    }

    @Override
    public Boolean updateProductInfoBySku(String sku, String name, String description, Double weight, Double length, Double width, Double height) {
        return lambdaUpdate()
                .eq(Product::getSku,sku)
                .set(Product::getName,name)
                .set(Product::getDescription,description)
                .set(Product::getWeight,weight)
                .set(Product::getLength,length)
                .set(Product::getWidth, width)
                .set(Product::getHeight, height)
                .update();
    }

    @Override
    public List<Product> findProductsBySkus(List<String> skus) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("sku", skus);
        return productMapper.selectList(queryWrapper);
    }
}