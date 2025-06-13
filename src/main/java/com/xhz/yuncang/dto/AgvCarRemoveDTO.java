package com.xhz.yuncang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * AGV搬运车移除数据传输对象
 * 用于接收前端删除AGV搬运车的请求数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AgvCarRemoveDTO {
    /**
     * 搬运车编号（唯一标识）
     */
    private String carNumber;
}