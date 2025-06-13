package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.FactoryConfig;
import com.xhz.yuncang.mapper.FactoryConfigMapper;
import com.xhz.yuncang.service.FactoryConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactoryConfigServiceImpl extends ServiceImpl<FactoryConfigMapper, FactoryConfig>implements FactoryConfigService {


    @Override
    public FactoryConfig getOne(){
            return lambdaQuery()
                    .eq(FactoryConfig::getId,1)
                    .one();
    }
}
