/*!40101 SET NAMES utf8 */;
-- 创建商品表
CREATE TABLE product
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL COMMENT '商品名称（如钢筋、木材等）',
    sku         VARCHAR(255)   NOT NULL UNIQUE COMMENT '商品编号，格式：product-001',
    description VARCHAR(255)   NULL COMMENT '商品描述',
    weight      DECIMAL(10, 2) NOT NULL COMMENT '重量（千克）',
    length      DECIMAL(10, 2) NOT NULL COMMENT '长度（厘米）',
    width       DECIMAL(10, 2) NOT NULL COMMENT '宽度（厘米）',
    height      DECIMAL(10, 2) NOT NULL COMMENT '高度（厘米）'
)
    COMMENT '商品信息表（存储钢材、木材等建筑材料信息）';

-- 创建入库单表
CREATE TABLE inbound_order
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_name   VARCHAR(255) NOT NULL COMMENT '入库单名称（如钢筋入库）',
    order_number VARCHAR(255) NOT NULL UNIQUE COMMENT '入库单编号，格式：inbound_order-001',
    create_time   DATETIME         NULL COMMENT '入库日期',
    user_id      VARCHAR(255) NULL COMMENT '负责人ID，引用user表',
    status       VARCHAR(255) NOT NULL COMMENT '状态（未开始、进行中、已完成）'
)
    COMMENT '入库单表（记录建筑材料入库信息）';

-- 创建入库单明细表
CREATE TABLE inbound_order_detail
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL COMMENT '入库单编号，引用inbound_order表',
    sku          VARCHAR(255) NOT NULL COMMENT '商品编号，引用product表',
    quantity     BIGINT       NOT NULL COMMENT '商品数量'
)
    COMMENT '入库单明细表（记录入库单的商品详情）';

-- 创建货架层表
CREATE TABLE storage_shelf
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    shelf_code VARCHAR(255)   NOT NULL UNIQUE COMMENT '货架编号，格式：shelf-001',
    max_weight DECIMAL(10, 2) NOT NULL COMMENT '最大载重（千克）',
    length     DECIMAL(10, 2) NOT NULL COMMENT '长度（厘米）',
    width      DECIMAL(10, 2) NOT NULL COMMENT '宽度（厘米）',
    height     DECIMAL(10, 2) NOT NULL COMMENT '高度（厘米）',
    location_x DECIMAL(10, 2) NOT NULL COMMENT '货架中心X坐标（厘米）',
    location_y DECIMAL(10, 2) NOT NULL COMMENT '货架中心Y坐标（厘米）',
    location_z DECIMAL(10, 2) NOT NULL COMMENT '货架中心Z坐标（厘米）'
)
    COMMENT '货架层表（存储建筑材料货架信息）';

-- 创建货架库存表
CREATE TABLE shelf_inventory
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    shelf_code VARCHAR(255) NOT NULL COMMENT '货架编号，引用storage_shelf表',
    sku        VARCHAR(255) NOT NULL COMMENT '商品编号，引用product表',
    quantity   BIGINT       NOT NULL COMMENT '商品数量'
)
    COMMENT '货架库存表（记录货架上的建筑材料库存）';

-- 创建销售订单表
CREATE TABLE sales_order
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL UNIQUE COMMENT '订单编号，格式：order-001',
    user_id      VARCHAR(255) NOT NULL COMMENT '客户ID，引用user表',
    create_time   DATETIME         NULL COMMENT '订单日期'
)
    COMMENT '销售订单表（记录建筑材料销售订单）';

-- 创建销售订单明细表
CREATE TABLE sales_order_detail
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL COMMENT '订单编号，引用sales_order表',
    sku          VARCHAR(255) NOT NULL COMMENT '商品编号，引用product表',
    quantity     BIGINT       NOT NULL COMMENT '商品数量'
)
    COMMENT '销售订单明细表（记录订单的商品详情）';

-- 创建出库单表
CREATE TABLE outbound_order
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL COMMENT '订单编号，引用sales_order表',
    planned_date DATE         NULL COMMENT '预计出库日期',
    user_id      VARCHAR(255) NULL COMMENT '操作员ID，引用user表',
    status       VARCHAR(255) NOT NULL COMMENT '状态（未开始、进行中、已完成）'
)
    COMMENT '出库单表（记录建筑材料出库信息）';

-- 创建库存表
CREATE TABLE inventory
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku      VARCHAR(255) NOT NULL COMMENT '商品编号，引用product表',
    quantity BIGINT       NOT NULL COMMENT '总库存量'
)
    COMMENT '库存表（记录建筑材料总库存）';

-- 创建AGV小车表
CREATE TABLE agv_car
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_number    VARCHAR(255)   NOT NULL UNIQUE COMMENT '小车编号，格式：car-001',
    status        VARCHAR(255)   NOT NULL COMMENT '状态（空闲、任务中、维护中）',
    battery_level INT            NOT NULL COMMENT '电量百分比（0-100）',
    sku           VARCHAR(255)   NOT NULL COMMENT '商品编号，引用product表',
    quantity      BIGINT         NOT NULL COMMENT '运输商品数量',
    start_x       DECIMAL(10, 2) NULL COMMENT '起始X坐标（厘米）',
    start_y       DECIMAL(10, 2) NULL COMMENT '起始Y坐标（厘米）',
    end_x         DECIMAL(10, 2) NULL COMMENT '终点X坐标（厘米）',
    end_y         DECIMAL(10, 2) NULL COMMENT '终点Y坐标（厘米）',
    user_id       VARCHAR(255)   NULL COMMENT '负责人ID，引用user表',
    max_weight    DECIMAL(10, 2) NOT NULL COMMENT '最大载重（千克）'
)
    COMMENT 'AGV小车表（记录运输建筑材料的AGV小车信息）';

alter table agv_car
    add create_time datetime null;

alter table agv_car
    add update_time datetime null;

alter table agv_car
    add end_z decimal(10, 2) null comment '目标货架层数';

alter table agv_car
    add location_x decimal(10, 2) not null comment '小车停放点X';

alter table agv_car
    add location_y decimal(10, 2) not null comment '小车停放点Y';




-- 创建用户表
CREATE TABLE user
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id   VARCHAR(255) NOT NULL UNIQUE COMMENT '用户ID，格式：user-001',
    username  VARCHAR(255) NOT NULL UNIQUE COMMENT '用户名',
    user_type VARCHAR(255) NOT NULL COMMENT '用户类型（管理员、操作员、客户）',
    password  VARCHAR(255) NOT NULL COMMENT '密码',
    nickname  VARCHAR(255) NULL COMMENT '昵称',
    phone     VARCHAR(255)  NULL COMMENT '邮箱',
    gender    VARCHAR(10)  NULL COMMENT '性别（男、女）'
)
    COMMENT '用户表（记录仓库管理员、操作员和客户信息）';


create table remind
(
    id      bigint auto_increment primary key ,
    status  varchar(255) not null,
    message varchar(255) not null comment '标题',
    context varchar(255) null comment '内容',
    create_time DATETIME not null,
    processed varchar(10) not null comment '是否处理过'
)
    comment '提示功能';


create table factory_config
(
    id               bigint auto_increment primary key ,
    allow_collision   varchar(10)    default '否'       not null,
    weight_ratio     varchar(20)    default '1/1/2'    not null,
    path_strategy    varchar(255)   default 'balanced' not null,
    max_layer        int            default 10         not null,
    max_layer_weight decimal(10,2)       default 3000       not null,
    max_shelf_number bigint         default 540        not null,
    max_car_weight   decimal(10,2)            default 1000       not null comment '小车最大承重',
    in_and_out_time  int            default 2          not null comment '出入库货物用时',
    car_speed        decimal(10, 2) default 1          not null
)
    comment '全局配置';


INSERT INTO user (user_id, username, user_type, password, nickname, phone, gender) VALUES
    ('user-admin', 'admin', '管理员', '$2a$10$HFCSIoEWvdrPrCYQ7VJKlOWDuFL0ZalV/Izk.3OLU9O1WHWhno4J.', 'admin', 'yanzhe_xiao@foxmail.com', '男')