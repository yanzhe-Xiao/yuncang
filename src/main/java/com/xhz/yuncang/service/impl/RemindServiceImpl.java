package com.xhz.yuncang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhz.yuncang.entity.Remind;

import com.xhz.yuncang.mapper.RemindMapper;
import com.xhz.yuncang.service.RemindService;
import com.xhz.yuncang.utils.Constants;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class RemindServiceImpl extends ServiceImpl<RemindMapper,Remind> implements RemindService{

    @Override
    public Boolean saveRemind(Remind remind){
        return  save(remind);
    }

    @Override
    public Boolean removeById(Long id){
        return lambdaUpdate()
                .eq(Remind::getId,id)
                .remove();
    }

    // 获取以id倒序排列的列表
    @Override
    public List<Remind> listRemindsDesc() {
        return lambdaQuery()
                .eq(Remind::getProcessed,"0")
                .orderByDesc(Remind::getId)
                .list();
    }

    @Override
    public List<Remind> listAllReminds() {
        return lambdaQuery()
                .orderByDesc(Remind::getId)
                .list();
    }

    @Override
    public List<Remind> listErrorReminds(){
        return lambdaQuery()
                .eq(Remind::getStatus, Constants.REMIND_ERROR)
                .orderByDesc(Remind::getId)
                .list();
    }
    @Override
    public List<Remind> listWarningReminds(){
        return lambdaQuery()
                .eq(Remind::getStatus, Constants.REMIND_WARNING)
                .orderByDesc(Remind::getId)
                .list();
    }
}



