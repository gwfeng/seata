# springcloud-nacos-seata

**分布式事务组件seata的使用demo，AT模式，集成nacos、springboot、springcloud、mybatis-plus，数据库采用mysql**

demo中使用的相关版本号，具体请看代码。如果搭建个人demo不成功，验证是否是由版本导致，由于目前这几个项目更新比较频繁，版本稍有变化便会出现许多奇怪问题

server 端
* seata server 1.4.0
* nacos server 1.2.1
  
client 端
* seata-client 1.4.0
* nacos-client 1.1.4
* spring-cloud-alibaba-seata 2.1.0.RELEASE
* spring-cloud-starter-alibaba-nacos-discovery 0.9.0.RELEASE
* springboot 2.0.6.RELEASE
* springcloud Finchley.RELEASE

----------

## 1. 服务端配置

### 1.1 Nacos-server

版本为nacos-server-1.2.1，demo采用本地单机部署方式，请参考 [Nacos 快速开始](https://nacos.io/zh-cn/docs/quick-start.html)

### 1.2 Seata-server

seata-server为release版本1.4.0，demo采用本地单机部署，从此处下载 [https://github.com/seata/seata/releases](https://github.com/seata/seata/releases)
并解压

##2、开发步骤：
#### 2.1、启动nacos 

#### 2.2 修改seata server registy.conf 配置

设置type、设置serverAddr为你的nacos节点地址。

**注意这里有一个坑，serverAddr不能带‘http://’前缀**

~~~java
registry {
        # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
        type = "nacos"
        loadBalance = "RandomLoadBalance"
        loadBalanceVirtualNodes = 10

        nacos {
        application = "seata-server"
        serverAddr = "127.0.0.1:8848"
        group = "SEATA_GROUP"
        namespace = "6ad3f55c-62a5-4cb2-a020-ef5b544ba310"
        cluster = "default"
        username = ""
        password = ""
        }

        config {
        # file、nacos 、apollo、zk、consul、etcd3
        type = "nacos"

        nacos {
        serverAddr = "127.0.0.1:8848"
        namespace = "6ad3f55c-62a5-4cb2-a020-ef5b544ba310"
        group = "SEATA_GROUP"
        username = ""
        password = ""
        }
}
~~~

#### 2.3 修改conf/nacos-config.txt 配置

service.vgroup_mapping.${your-service-gruop}=default，中间的${your-service-gruop}为自己定义的服务组名称，服务中的application.properties文件里配置服务组名称。
seata 高版本采用的是  service.vgroupMapping
demo中有两个服务，分别是stock-service和order-service，所以配置如下

~~~properties
service.vgroupMapping.order-service-group=default
service.vgroupMapping.stock-service-group=default
~~~

** 注意这里,高版本中应该是vgroupMapping 同时后面的如: order-service-group 不能定义为 order_service_group**

#### 2.4 修改需要上传到nacos 上的配置
~~~
service.vgroupMapping.my_test_tx_group=default
service.vgroupMapping.order-service-group=default
service.vgroupMapping.stock-service-group=default

store.mode=db

store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.jdbc.Driver
store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true
store.db.user=root
store.db.password=root  
~~~
#### 2.5、推送配置到nacos
切换到 /script/config-center/nacos/ 路径下
gitbash 窗口执行
~~~
sh nacos-config.sh -h localhost -p 8848 -g SEATA_GROUP -t 6ad3f55c-62a5-4cb2-a020-ef5b544ba310
~~~

#### 2.6、启动seata server

#### 2.7、配置seata 客户端
##### 2.7.1、registry.conf文件
~~~
registry {
# file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
type = "nacos"
loadBalance = "RandomLoadBalance"
loadBalanceVirtualNodes = 10
nacos {
application = "seata-server"
serverAddr = "nacos的ip地址:8848"
namespace = "df2011b0-ed94-4fd2-9a33-baa6f97f5af5"
group = "SEATA_GROUP"
cluster = "default"
}
}

config {
# file、nacos 、apollo、zk、consul、etcd3
type = "nacos"
nacos {
serverAddr = "nacos的ip地址:8848"
namespace = "df2011b0-ed94-4fd2-9a33-baa6f97f5af5"
group = "SEATA_GROUP"
}

}
~~~

##### 2.7.2、yml
~~~
spring.application.name=order-service
server.port=9091
# Nacos 注册中心地址
spring.cloud.nacos.discovery.server-addr=localhost:8848
spring.cloud.nacos.discovery.namespace=6ad3f55c-62a5-4cb2-a020-ef5b544ba310
# seata 服务分组，要与服务端nacos-config.txt中service.vgroup_mapping的后缀对应
spring.cloud.alibaba.seata.tx-service-group=order-service-group
logging.level.io.seata=debug
# 数据源配置
spring.datasource.druid.url=jdbc:mysql://localhost:3306/seata_order?allowMultiQueries=true
spring.datasource.druid.driverClassName=com.mysql.jdbc.Driver
spring.datasource.druid.username=root
spring.datasource.druid.password=root
~~~

##### 2.8、启动java 服务

##### 2.9、开启验证
----------

## 2. 应用配置

### 2.1 数据库初始化

~~~SQL
#### 2.1.1 全局事务表
~~~
-- -------------------------------- The script used when storeMode is 'db' --------------------------------
-- the table to store GlobalSession data
CREATE TABLE IF NOT EXISTS `global_table`
(
`xid`                       VARCHAR(128) NOT NULL,
`transaction_id`            BIGINT,
`status`                    TINYINT      NOT NULL,
`application_id`            VARCHAR(32),
`transaction_service_group` VARCHAR(32),
`transaction_name`          VARCHAR(128),
`timeout`                   INT,
`begin_time`                BIGINT,
`application_data`          VARCHAR(2000),
`gmt_create`                DATETIME,
`gmt_modified`              DATETIME,
PRIMARY KEY (`xid`),
KEY `idx_gmt_modified_status` (`gmt_modified`, `status`),
KEY `idx_transaction_id` (`transaction_id`)
) ENGINE = InnoDB
DEFAULT CHARSET = utf8;

-- the table to store BranchSession data
CREATE TABLE IF NOT EXISTS `branch_table`
(
`branch_id`         BIGINT       NOT NULL,
`xid`               VARCHAR(128) NOT NULL,
`transaction_id`    BIGINT,
`resource_group_id` VARCHAR(32),
`resource_id`       VARCHAR(256),
`branch_type`       VARCHAR(8),
`status`            TINYINT,
`client_id`         VARCHAR(64),
`application_data`  VARCHAR(2000),
`gmt_create`        DATETIME(6),
`gmt_modified`      DATETIME(6),
PRIMARY KEY (`branch_id`),
KEY `idx_xid` (`xid`)
) ENGINE = InnoDB
DEFAULT CHARSET = utf8;

-- the table to store lock data
CREATE TABLE IF NOT EXISTS `lock_table`
(
`row_key`        VARCHAR(128) NOT NULL,
`xid`            VARCHAR(128),
`transaction_id` BIGINT,
`branch_id`      BIGINT       NOT NULL,
`resource_id`    VARCHAR(256),
`table_name`     VARCHAR(32),
`pk`             VARCHAR(36),
`gmt_create`     DATETIME,
`gmt_modified`   DATETIME,
PRIMARY KEY (`row_key`),
KEY `idx_branch_id` (`branch_id`)
) ENGINE = InnoDB
DEFAULT CHARSET = utf8;
~~~

#### 2.1.1 业务表+undo表
~~~
-- 创建 order库、业务表、undo_log表
create database seata_order;
use seata_order;

DROP TABLE IF EXISTS `order_tbl`;
CREATE TABLE `order_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255) DEFAULT NULL,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT 0,
  `money` int(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)   NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  `ext`           VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;


-- 创建 stock库、业务表、undo_log表
create database seata_stock;
use seata_stock;

DROP TABLE IF EXISTS `stock_tbl`;
CREATE TABLE `stock_tbl` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commodity_code` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`commodity_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `undo_log`
(
  `id`            BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `branch_id`     BIGINT(20)   NOT NULL,
  `xid`           VARCHAR(100) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT(11)      NOT NULL,
  `log_created`   DATETIME     NOT NULL,
  `log_modified`  DATETIME     NOT NULL,
  `ext`           VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

-- 初始化库存模拟数据
INSERT INTO seata_stock.stock_tbl (id, commodity_code, count) VALUES (1, 'product-1', 10);
INSERT INTO seata_stock.stock_tbl (id, commodity_code, count) VALUES (2, 'product-2', 10);
~~~

### 2.2 应用配置

见代码

几个重要的配置

1. 每个应用的resource里需要配置一个registry.conf ，demo中与seata-server里的配置相同
2. application.propeties 的各个配置项，注意spring.cloud.alibaba.seata.tx-service-group 是服务组名称，与nacos-config.txt
   配置的service.vgroup_mapping.${your-service-gruop}具有对应关系

----------

## 3. 测试

1. 分布式事务成功，模拟正常下单、扣库存

   localhost:9091/order/placeOrder/commit

2. 分布式事务失败，模拟下单成功、扣库存失败，最终同时回滚

   localhost:9091/order/placeOrder/rollback 





