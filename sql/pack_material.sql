-- =============================================
-- 包装物料管理系统数据库脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS pack_material DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pack_material;

-- =============================================
-- 1. 包装物料表 - 物料档案
-- =============================================
DROP TABLE IF EXISTS pack_material;
CREATE TABLE pack_material (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    specification VARCHAR(200) COMMENT '规格型号',
    material_type VARCHAR(50) COMMENT '物料类型：纸箱、泡沫、胶带、标签等',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位：个、卷、米等',
    warehouse_location VARCHAR(100) COMMENT '库位',
    safety_stock INT NOT NULL DEFAULT 0 COMMENT '安全库存',
    supplier VARCHAR(100) COMMENT '供应商',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_material_code (material_code),
    KEY idx_material_name (material_name),
    KEY idx_material_type (material_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='包装物料表';

-- =============================================
-- 2. 物料库存表 - 实时库存
-- =============================================
DROP TABLE IF EXISTS pack_inventory;
CREATE TABLE pack_inventory (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    current_stock INT NOT NULL DEFAULT 0 COMMENT '当前库存',
    locked_stock INT NOT NULL DEFAULT 0 COMMENT '锁定库存（已分配未扣减）',
    available_stock INT NOT NULL DEFAULT 0 COMMENT '可用库存（当前库存-锁定库存）',
    last_in_time DATETIME COMMENT '最近入库时间',
    last_out_time DATETIME COMMENT '最近出库时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_material_id (material_id),
    KEY idx_available_stock (available_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物料库存表';

-- =============================================
-- 3. 出入库记录表
-- =============================================
DROP TABLE IF EXISTS pack_stock_record;
CREATE TABLE pack_stock_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    record_no VARCHAR(50) NOT NULL COMMENT '单据编号',
    record_type TINYINT NOT NULL COMMENT '记录类型：1-采购入库，2-领用出库，3-报工核减，4-盘盈入库，5-盘亏出库',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    quantity INT NOT NULL COMMENT '数量（入库为正，出库为负）',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    operator VARCHAR(50) COMMENT '操作人',
    operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    related_order_no VARCHAR(50) COMMENT '关联单据号（采购单、报工单等）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_record_no (record_no),
    KEY idx_material_id (material_id),
    KEY idx_record_type (record_type),
    KEY idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出入库记录表';

-- =============================================
-- 4. 成品表
-- =============================================
DROP TABLE IF EXISTS finished_product;
CREATE TABLE finished_product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_code VARCHAR(50) NOT NULL COMMENT '成品编码',
    product_name VARCHAR(100) NOT NULL COMMENT '成品名称',
    specification VARCHAR(200) COMMENT '规格型号',
    category VARCHAR(50) COMMENT '产品分类',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成品表';

-- =============================================
-- 5. 成品包装BOM表 - 定义每个成品需要哪些包装料及用量
-- =============================================
DROP TABLE IF EXISTS product_pack_bom;
CREATE TABLE product_pack_bom (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '成品ID',
    product_code VARCHAR(50) NOT NULL COMMENT '成品编码',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    dosage DECIMAL(10,2) NOT NULL COMMENT '单位成品用量',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_material (product_id, material_id),
    KEY idx_product_id (product_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成品包装BOM表';

-- =============================================
-- 6. 包装报工表
-- =============================================
DROP TABLE IF EXISTS pack_work_report;
CREATE TABLE pack_work_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    report_no VARCHAR(50) NOT NULL COMMENT '报工单号',
    product_id BIGINT NOT NULL COMMENT '成品ID',
    product_code VARCHAR(50) NOT NULL COMMENT '成品编码',
    product_name VARCHAR(100) NOT NULL COMMENT '成品名称',
    production_quantity INT NOT NULL COMMENT '生产数量',
    report_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报工时间',
    operator VARCHAR(50) COMMENT '操作人',
    work_line VARCHAR(50) COMMENT '包装线',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待核减，2-已核减，3-已取消',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_report_no (report_no),
    KEY idx_product_id (product_id),
    KEY idx_report_time (report_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='包装报工表';

-- =============================================
-- 7. 报工用料明细表 - 记录报工实际核减的物料
-- =============================================
DROP TABLE IF EXISTS work_report_material;
CREATE TABLE work_report_material (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    report_id BIGINT NOT NULL COMMENT '报工单ID',
    report_no VARCHAR(50) NOT NULL COMMENT '报工单号',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    standard_quantity DECIMAL(12,2) NOT NULL COMMENT '标准用量（定额）',
    actual_quantity DECIMAL(12,2) NOT NULL COMMENT '实际用量',
    difference_quantity DECIMAL(12,2) NOT NULL COMMENT '用量差异（实际-标准）',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_report_id (report_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报工用料明细表';

-- =============================================
-- 8. 请购清单表
-- =============================================
DROP TABLE IF EXISTS purchase_request;
CREATE TABLE purchase_request (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    request_no VARCHAR(50) NOT NULL COMMENT '请购单号',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    specification VARCHAR(200) COMMENT '规格型号',
    current_stock INT NOT NULL COMMENT '当前库存',
    safety_stock INT NOT NULL COMMENT '安全库存',
    suggested_quantity INT NOT NULL COMMENT '建议采购量',
    unit VARCHAR(20) NOT NULL COMMENT '计量单位',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待处理，2-已采购，3-已关闭',
    request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请购时间',
    operator VARCHAR(50) COMMENT '操作人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_request_no (request_no),
    KEY idx_material_id (material_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请购清单表';

-- =============================================
-- 初始化测试数据
-- =============================================

-- 插入包装物料数据
INSERT INTO pack_material (material_code, material_name, specification, material_type, unit, warehouse_location, safety_stock, supplier, remark) VALUES
('PM001', '纸箱A', '400*300*200mm', '纸箱', '个', 'A-01-01', 500, '纸箱厂A', '标准外箱'),
('PM002', '纸箱B', '300*200*150mm', '纸箱', '个', 'A-01-02', 300, '纸箱厂A', '小外箱'),
('PM003', 'EPS泡沫板', '10mm厚', '泡沫', '片', 'B-02-01', 1000, '泡沫厂B', '缓冲材料'),
('PM004', 'EPE珍珠棉', '5mm厚', '泡沫', '卷', 'B-02-02', 50, '泡沫厂B', '缓冲材料'),
('PM005', '封箱胶带', '48mm*100m', '胶带', '卷', 'C-03-01', 200, '胶带厂C', '透明封箱'),
('PM006', '警示胶带', '48mm*50m', '胶带', '卷', 'C-03-02', 100, '胶带厂C', '黄黑警示'),
('PM007', '产品标签', '100*50mm', '标签', '张', 'D-04-01', 5000, '印刷厂D', '不干胶'),
('PM008', '外箱标签', '150*100mm', '标签', '张', 'D-04-02', 3000, '印刷厂D', '外箱唛头');

-- 初始化库存
INSERT INTO pack_inventory (material_id, material_code, current_stock, locked_stock, available_stock) VALUES
(1, 'PM001', 800, 0, 800),
(2, 'PM002', 200, 0, 200),
(3, 'PM003', 1500, 0, 1500),
(4, 'PM004', 30, 0, 30),
(5, 'PM005', 250, 0, 250),
(6, 'PM006', 80, 0, 80),
(7, 'PM007', 6000, 0, 6000),
(8, 'PM008', 2000, 0, 2000);

-- 插入成品数据
INSERT INTO finished_product (product_code, product_name, specification, category, unit, remark) VALUES
('P001', '智能音箱Pro', '黑色 10W', '智能设备', '台', '高端智能音箱'),
('P002', '智能音箱Mini', '白色 5W', '智能设备', '台', '入门级智能音箱'),
('P003', '无线耳机', '主动降噪', '音频设备', '副', 'TWS蓝牙耳机'),
('P004', '智能手环', '心率监测', '可穿戴', '个', '运动健康手环');

-- 插入成品包装BOM
INSERT INTO product_pack_bom (product_id, product_code, material_id, material_code, material_name, dosage, unit, remark) VALUES
-- P001: 智能音箱Pro的包装
(1, 'P001', 1, 'PM001', '纸箱A', 1.00, '个', '每台1个外箱'),
(1, 'P001', 3, 'PM003', 'EPS泡沫板', 2.00, '片', '上下各1片'),
(1, 'P001', 5, 'PM005', '封箱胶带', 0.10, '卷', '封箱用'),
(1, 'P001', 7, 'PM007', '产品标签', 1.00, '张', '机身标签'),
(1, 'P001', 8, 'PM008', '外箱标签', 1.00, '张', '外箱唛头'),
-- P002: 智能音箱Mini的包装
(2, 'P002', 2, 'PM002', '纸箱B', 1.00, '个', '每台1个外箱'),
(2, 'P002', 4, 'PM004', 'EPE珍珠棉', 0.50, '卷', '缓冲包裹'),
(2, 'P002', 5, 'PM005', '封箱胶带', 0.05, '卷', '封箱用'),
(2, 'P002', 7, 'PM007', '产品标签', 1.00, '张', '机身标签'),
(2, 'P002', 8, 'PM008', '外箱标签', 1.00, '张', '外箱唛头'),
-- P003: 无线耳机的包装
(3, 'P003', 2, 'PM002', '纸箱B', 1.00, '个', '每副1个外箱'),
(3, 'P003', 4, 'PM004', 'EPE珍珠棉', 0.30, '卷', '内盒缓冲'),
(3, 'P003', 5, 'PM005', '封箱胶带', 0.05, '卷', '封箱用'),
(3, 'P003', 7, 'PM007', '产品标签', 1.00, '张', '机身标签'),
-- P004: 智能手环的包装
(4, 'P004', 2, 'PM002', '纸箱B', 1.00, '个', '每个1个外箱'),
(4, 'P004', 3, 'PM003', 'EPS泡沫板', 1.00, '片', '内衬'),
(4, 'P004', 5, 'PM005', '封箱胶带', 0.05, '卷', '封箱用'),
(4, 'P004', 7, 'PM007', '产品标签', 1.00, '张', '机身标签');

-- 插入请购清单（PM004库存低于安全库存）
INSERT INTO purchase_request (request_no, material_id, material_code, material_name, specification, current_stock, safety_stock, suggested_quantity, unit, operator, remark) VALUES
('PR20240601001', 4, 'PM004', 'EPE珍珠棉', '5mm厚', 30, 50, 100, '卷', 'system', '库存低于安全线，请及时采购');

-- =============================================
-- 脚本执行完成
-- =============================================
