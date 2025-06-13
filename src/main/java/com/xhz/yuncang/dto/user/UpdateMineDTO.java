package com.xhz.yuncang.dto.user;

import lombok.Data;

/**
 * <p>Package Name: com.xhz.yuncang.dto.user </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/6/9 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since jdk-21
 */
@Data
public class UpdateMineDTO {
    // email 也是 phone 字段
    private String email;
    private String username;
    private String nickname;
    private String gender;
}
