package com.xhz.yuncang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xhz.yuncang.entity.Remind;

import java.util.List;

public interface RemindService extends IService<Remind> {
    Boolean saveRemind(Remind remind);

    Boolean removeById(Long id);

    // 获取以id倒序排列的列表
    List<Remind> listRemindsDesc();

    List<Remind> listAllReminds();

    List<Remind> listErrorReminds();

    List<Remind> listWarningReminds();
}
