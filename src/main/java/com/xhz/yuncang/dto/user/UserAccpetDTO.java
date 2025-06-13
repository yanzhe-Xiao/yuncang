package com.xhz.yuncang.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <p>Package Name: com.xhz.yuncang.dto.user </p>
 * <p>Description: 传输是否通义该用户注册为操作员或管理员的DTO </p>
 * <p>Create Time: 2025/6/8 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since jdk-21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserAccpetDTO {
    private String userName;
    private boolean agree;
}
