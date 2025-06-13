package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.AgvCar;

import java.util.List;

public interface AgvCarService extends IService<AgvCar> {

    AgvCar findByCarNumber(String carNumber);

    AgvCar findById(Long id);

    Boolean addOneAgvCar(AgvCar agvCar);

    Boolean deleteByCarNumber(String carNumber);

    Boolean deleteById(Long id);

    List<AgvCar> findAll();

    Boolean deleteAll();

    Boolean updateByCarNumber(AgvCar agvCar);

    boolean updateById(AgvCar agvCar);
}