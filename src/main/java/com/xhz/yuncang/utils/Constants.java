package com.xhz.yuncang.utils;

import org.apache.el.parser.Token;

public interface Constants {

    /**
     * 未设置7天免登录，jwt 过期时间设为：0.25 hours=15 minutes
     */
//    public static final Integer TOKEN_NO_REM_TTL=480;
    /**
     * 每15min更新一次token，减小前端localStorage泄露风险
     */
    public static final Integer TOKEN_TTL=480;
    /**
     * 设置7天免登录，jwt 过期时间设为：7天=10080 minutes
     */
//    public static final Integer TOKEN_REM_TTL=10080;
    /**
     * 未设置7天免登录，redis 存储token的时间时30 minutes
     */
    public static final Integer REDIS_NO_REM_TTL=30;
    /**
     * 设置7天免登录，redis 存储token的时间时7天=10080 minutes
     */
    public static final Integer REDIS_REM_TTL=10080;
    /**
     * redis中登录token存储的前缀
     */
    public static final String LOGIN_TOKEN_PREFIX="login:token:";

    public static final String TOKEN_TRANS_PREFIX="Bearer ";
    /**
     * 一页显示10条数据
     */
    public static final Integer PageSize=10;

    /**
     * 订单待开始状态
     */
    public static final String STATUS_ORDER_TOSTART = "未开始";

    /**
     * 订单进行中状态
     */
    public static final String STATUS_ORDER_ONGOING = "进行中";

    /**
     * 订单已完成状态
     */
    public static final String STATUS_ORDER_FINISHED = "已完成";


    public static final String CAR_STATUS_ONGOING= "行驶中";

    public static final String CAR_STATUS_REPAIR="维护中";

    public static final String CAR_STATUS_FREE="空闲中";

    public static final String USR_ADMIN="管理员";

    public static final String USER_CUSTOMER="客户";

    public static final String USER_OPERATOR="操作员";

    public static final String SENDER_EMAIL = "xiao2397903439@163.com";

    public static final String EMAIL_VERIFICATION_CODE_KEY_PREFIX = "verification_code";
    public static final long EMAIL_VERIFICATION_CODE_EXPIRATION_MINUTES = 5;

    public static final String REMIND_ERROR="error";

    public static final String REMIND_SUCCESS="success";

    public static final String REMIND_WARNING="warning";

    public static final String REMIND_INFO="info";

    public static final int PATH_CANNOT_GO=1;

    public static final int PATH_CAN_GO=0;

    ////////////////////////////////////
    //全局变量
    /**
     * 移动难度比率
     */
    public  static final int MOVE_DIFFER_X=1;

    public static final int MOVE_DIFFER_Y=1;

    public static final int MOVE_DIFFER_Z=2;

    /**
     * 是否处理冲突？
     * ，即小车是否避免重合？
     */
    public static  final String DEALING_WITH_CONFLICTS="是";

    /**
     * 有个判断：如果是1、“短路径优先”，则给：“路径 + 库存数量”双因素加权评分的路径权重高点
     * 			     2、  “多数量优先”， 则给："路径+库存数量"双因素加权评分的库存权重高点
     * 			     3、  “系统平衡策略”，
     * 若：
     * 物品少、数量大 →  则给："路径+库存数量"双因素加权评分的库存权重高点
     *
     * 物品多、分散 →  则给：“路径 + 库存数量”双因素加权评分的路径权重高点
     *可选: "short-path", "more-stock", "balanced","system-judged"
     */
      public static final String OUT_PREFER="system-judged";


    /**
     * 出入库的交互点的交互时间为2s
     */
    public static final int INTERACTION=2;
    ////////////////////////////////////
}
