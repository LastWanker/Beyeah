-- 实际上不影响运行。
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
-- Table structure for beyeah_mall_admin_user
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_admin_user`;
CREATE TABLE `beyeah_mall_admin_user`  (
                                           `admin_user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '管理员id',
                                           `login_user_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '管理员登陆名称',
                                           `login_password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '管理员登陆密码',
                                           `nick_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '管理员显示昵称',
                                           `locked` tinyint(4) NULL DEFAULT 0 COMMENT '是否锁定 0未锁定 1已锁定无法登陆',
                                           PRIMARY KEY (`admin_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Records of beyeah_mall_admin_user
-- ----------------------------
INSERT INTO `beyeah_mall_admin_user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '大哥', 0);
INSERT INTO `beyeah_mall_admin_user` VALUES (2, 'beyeah-admin1', 'e10adc3949ba59abbe56e057f20f883e', '小弟01', 0);
INSERT INTO `beyeah_mall_admin_user` VALUES (3, 'beyeah-admin2', 'e10adc3949ba59abbe56e057f20f883e', '小弟02', 0);
-- Table structure for beyeah_mall_carousel
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_carousel`;
CREATE TABLE `beyeah_mall_carousel`  (
                                         `carousel_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '首页轮播图主键id',
                                         `carousel_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '轮播图',
                                         `redirect_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '\'##\'' COMMENT '点击后的跳转地址(默认不跳转)',
                                         `carousel_rank` int(11) NOT NULL DEFAULT 0 COMMENT '排序值(字段越大越靠前)',
                                         `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
                                         `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `create_user` int(11) NOT NULL DEFAULT 0 COMMENT '创建者id',
                                         `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                         `update_user` int(11) NOT NULL DEFAULT 0 COMMENT '修改者id',
                                         PRIMARY KEY (`carousel_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Records of beyeah_mall_carousel
-- ----------------------------
-- Table structure for beyeah_mall_goods_category
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_goods_category`;
CREATE TABLE `beyeah_mall_goods_category`  (
                                               `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类id',
                                               `category_level` tinyint(4) NOT NULL DEFAULT 0 COMMENT '分类级别(1-一级分类 2-二级分类 3-三级分类)',
                                               `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父分类id',
                                               `category_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '分类名称',
                                               `category_rank` int(11) NOT NULL DEFAULT 0 COMMENT '排序值(字段越大越靠前)',
                                               `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
                                               `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               `create_user` int(11) NOT NULL DEFAULT 0 COMMENT '创建者id',
                                               `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
                                               `update_user` int(11) NULL DEFAULT 0 COMMENT '修改者id',
                                               PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Records of beyeah_mall_goods_category
-- ----------------------------
-- Table structure for beyeah_mall_goods_info
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_goods_info`;
CREATE TABLE `beyeah_mall_goods_info`  (
                                           `goods_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品表主键id',
                                           `goods_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '商品名',
                                           `goods_intro` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '商品简介',
                                           `goods_category_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '关联分类id',
                                           `goods_cover_img` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '/admin/dist/img/no-img.png' COMMENT '商品主图',
                                           `goods_carousel` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '/admin/dist/img/no-img.png' COMMENT '商品轮播图',
                                           `goods_detail_content` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商品详情',
                                           `original_price` int(11) NOT NULL DEFAULT 1 COMMENT '商品价格',
                                           `selling_price` int(11) NOT NULL DEFAULT 1 COMMENT '商品实际售价',
                                           `stock_num` int(11) NOT NULL DEFAULT 0 COMMENT '商品库存数量',
                                           `tag` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '商品标签',
                                           `goods_sell_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '商品上架状态 0-下架 1-上架',
                                           `create_user` int(11) NOT NULL DEFAULT 0 COMMENT '添加者主键id',
                                           `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '商品添加时间',
                                           `update_user` int(11) NOT NULL DEFAULT 0 COMMENT '修改者主键id',
                                           `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '商品修改时间',
                                           PRIMARY KEY (`goods_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10896 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Records of beyeah_mall_goods_info
-- ---------------------------
-- Table structure for beyeah_mall_index_config
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_index_config`;
CREATE TABLE `beyeah_mall_index_config`  (
                                             `config_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '首页配置项主键id',
                                             `config_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '显示字符(配置搜索时不可为空，其他可为空)',
                                             `config_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1-搜索框热搜 2-搜索下拉框热搜 3-(首页)热销商品 4-(首页)新品上线 5-(首页)为你推荐',
                                             `goods_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '商品id 默认为0',
                                             `redirect_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '##' COMMENT '点击后的跳转地址(默认不跳转)',
                                             `config_rank` int(11) NOT NULL DEFAULT 0 COMMENT '排序值(字段越大越靠前)',
                                             `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
                                             `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `create_user` int(11) NOT NULL DEFAULT 0 COMMENT '创建者id',
                                             `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新修改时间',
                                             `update_user` int(11) NULL DEFAULT 0 COMMENT '修改者id',
                                             PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Records of beyeah_mall_index_config
-- ----------------------------
-- Table structure for beyeah_mall_order
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_order`;
CREATE TABLE `beyeah_mall_order`  (
                                      `order_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单表主键id',
                                      `order_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '订单号',
                                      `user_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '用户主键id',
                                      `total_price` int(11) NOT NULL DEFAULT 1 COMMENT '订单总价',
                                      `pay_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '支付状态:0.未支付,1.支付成功,-1:支付失败',
                                      `pay_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0.无 1.支付宝支付 2.微信支付',
                                      `pay_time` datetime(0) NULL DEFAULT NULL COMMENT '支付时间',
                                      `order_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功 -1.手动关闭 -2.超时关闭 -3.商家关闭',
                                      `extra_info` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '订单body',
                                      `user_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货人姓名',
                                      `user_phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货人手机号',
                                      `user_address` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货人收货地址',
                                      `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
                                      `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新修改时间',
                                      PRIMARY KEY (`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Records of beyeah_mall_order
-- ----------------------------
-- Table structure for beyeah_mall_order_item
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_order_item`;
CREATE TABLE `beyeah_mall_order_item`  (
                                           `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单关联购物项主键id',
                                           `order_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '订单主键id',
                                           `goods_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '关联商品id',
                                           `goods_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '下单时商品的名称(订单快照)',
                                           `goods_cover_img` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '下单时商品的主图(订单快照)',
                                           `selling_price` int(11) NOT NULL DEFAULT 1 COMMENT '下单时商品的价格(订单快照)',
                                           `goods_count` int(11) NOT NULL DEFAULT 1 COMMENT '数量(订单快照)',
                                           `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           PRIMARY KEY (`order_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Records of beyeah_mall_order_item
-- ----------------------------
-- Table structure for beyeah_mall_shopping_cart_item
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_shopping_cart_item`;
CREATE TABLE `beyeah_mall_shopping_cart_item`  (
                                                   `cart_item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '购物项主键id',
                                                   `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
                                                   `goods_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '关联商品id',
                                                   `goods_count` int(11) NOT NULL DEFAULT 1 COMMENT '数量(最大为5)',
                                                   `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
                                                   `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                   `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新修改时间',
                                                   PRIMARY KEY (`cart_item_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Table structure for beyeah_mall_user_address
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_user_address`;
CREATE TABLE `beyeah_mall_user_address`  (
                                             `address_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址主键id',
                                             `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
                                             `consignee` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货人姓名',
                                             `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货人手机号',
                                             `province` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '省份',
                                             `city` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '城市',
                                             `district` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '区县',
                                             `detail` varchar(120) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '详细地址',
                                             `is_default` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否默认地址(0-否 1-是)',
                                             `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
                                             `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新修改时间',
                                             PRIMARY KEY (`address_id`) USING BTREE,
                                             KEY `idx_user_deleted_default` (`user_id`,`is_deleted`,`is_default`) USING BTREE,
                                             KEY `idx_user_deleted_time` (`user_id`,`is_deleted`,`update_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Table structure for beyeah_mall_user
-- ----------------------------
DROP TABLE IF EXISTS `beyeah_mall_user`;
CREATE TABLE `beyeah_mall_user`  (
                                     `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户主键id',
                                     `nick_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '用户昵称',
                                     `login_name` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '登陆名称(默认为手机号)',
                                     `password_md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'MD5加密后的密码',
                                     `introduce_sign` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '个性签名',
                                     `address` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货地址',
                                     `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '注销标识字段(0-正常 1-已注销)',
                                     `locked_flag` tinyint(4) NOT NULL DEFAULT 0 COMMENT '锁定标识字段(0-未锁定 1-已锁定)',
                                     `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                                     PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;
-- Records of beyeah_mall_user
-- ----------------------------
