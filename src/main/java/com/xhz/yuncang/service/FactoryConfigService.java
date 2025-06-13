package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.FactoryConfig;

import java.util.List;

public interface FactoryConfigService extends IService<FactoryConfig> {

    FactoryConfig getOne();
}
