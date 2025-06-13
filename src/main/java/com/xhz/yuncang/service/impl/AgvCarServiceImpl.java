package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.AgvCar;
import com.xhz.yuncang.mapper.AgvCarMapper;
import com.xhz.yuncang.service.AgvCarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgvCarServiceImpl extends ServiceImpl<AgvCarMapper, AgvCar> implements AgvCarService {

    @Autowired
    private AgvCarMapper agvCarMapper;

    @Override
    public AgvCar findByCarNumber(String carNumber) {
        return lambdaQuery()
                .eq(AgvCar::getCarNumber, carNumber)
                .one();
    }

    @Override
    public AgvCar findById(Long id) {
        return lambdaQuery()
                .eq(AgvCar::getId,id)
                .one();
    }

    @Override
    public Boolean addOneAgvCar(AgvCar agvCar) {
        return save(agvCar);
    }

    @Override
    public Boolean deleteByCarNumber(String carNumber) {
        return lambdaUpdate()
                .eq(AgvCar::getCarNumber, carNumber)
                .remove();
    }

    @Override
    public Boolean deleteById(Long id) {
        return lambdaUpdate()
                .eq(AgvCar::getId,id)
                .remove();
    }

    @Override
    public List<AgvCar> findAll() {
        return list();
    }

    @Override
    public Boolean deleteAll() {
        return remove(null);
    }

    @Override
    public Boolean updateByCarNumber(AgvCar agvCar) {
        return lambdaUpdate()
                .eq(AgvCar::getCarNumber, agvCar.getCarNumber())
                .set(AgvCar::getStatus, agvCar.getStatus())
                .set(AgvCar::getBatteryLevel, agvCar.getBatteryLevel())
                .set(AgvCar::getSku, agvCar.getSku())
                .set(AgvCar::getQuantity, agvCar.getQuantity())
                .set(AgvCar::getStartX, agvCar.getStartX())
                .set(AgvCar::getStartY, agvCar.getStartY())
                .set(AgvCar::getEndX, agvCar.getEndX())
                .set(AgvCar::getEndY, agvCar.getEndY())
                .set(AgvCar::getUserId, agvCar.getUserId())
                .set(AgvCar::getMaxWeight, agvCar.getMaxWeight())
                .set(AgvCar::getEndZ,agvCar.getEndZ())
                .set(AgvCar::getLocationX,agvCar.getLocationX())
                .set(AgvCar::getLocationY,agvCar.getLocationY())
                .update();
    }

    @Override
    public boolean updateById(AgvCar agvCar) {
        return lambdaUpdate()
                .eq(AgvCar::getId,agvCar.getId())
                .set(AgvCar::getCarNumber, agvCar.getCarNumber())
                .set(AgvCar::getStatus, agvCar.getStatus())
                .set(AgvCar::getBatteryLevel, agvCar.getBatteryLevel())
                .set(AgvCar::getSku, agvCar.getSku())
                .set(AgvCar::getQuantity, agvCar.getQuantity())
                .set(AgvCar::getStartX, agvCar.getStartX())
                .set(AgvCar::getStartY, agvCar.getStartY())
                .set(AgvCar::getEndX, agvCar.getEndX())
                .set(AgvCar::getEndY, agvCar.getEndY())
                .set(AgvCar::getUserId, agvCar.getUserId())
                .set(AgvCar::getMaxWeight, agvCar.getMaxWeight())
                .set(AgvCar::getUpdateTime, LocalDateTime.now())
                .set(AgvCar::getEndZ,agvCar.getEndZ())
                .set(AgvCar::getLocationX,agvCar.getLocationX())
                .set(AgvCar::getLocationY,agvCar.getLocationY())
                .update();
    }
}