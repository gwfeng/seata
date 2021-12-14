# seata
seata 多种场景demo

启动Server
Server端存储模式（store.mode）现有file、db、redis三种（后续将引入raft,mongodb），file模式无需改动，直接启动即可，下面专门讲下db和redis启动步骤。
注： file模式为单机模式，全局事务会话信息内存中读写并持久化本地文件root.data，性能较高;

db模式为高可用模式，全局事务会话信息通过db共享，相应性能差些;

redis模式Seata-Server 1.3及以上版本支持,性能较高,存在事务信息丢失风险,请提前配置合适当前场景的redis持久化配置.


file、db、redis 只是服务端的不同模式
file 模式，server,client 端的配置文件都不需要修改，分别直接启动就行，file模式为单机模式，全局事务会话信息内存中读写并持久化本地文件root.data，性能较高;

db 模式，需要先添加db.sql 创建事务表 见mysql.sql文件 
db 模式，server 端需要修改 register.conf,file.conf 配置为db 模式，client端无需修改（有两种实现方式，采用不同的依赖包，seata-all 或者 seata-spring-boot-starter）
seata-all 依赖,resource 下需要添加register.conf,file.conf 文件，无需改动 demo "multiple-datasource-mybatis-plus"
#client配置注意点：
#file.conf
service {
  #transaction service group mapping
  vgroupMapping.my_test_tx_group = "default"
  #only support when registry.type=file, please don't set multiple addresses
  default.grouplist = "127.0.0.1:8091"
  #degrade, current not support
  enableDegrade = false
  #disable seata
  disableGlobalTransaction = false
}

#allication.properties
spring.cloud.alibaba.seata.tx-service-group=my_test_tx_group


seata-spring-boot-starter 依赖，无需添加register.conf,file.conf 文件,支持yml、properties配置(.conf可删除)，内部已依赖seata-all 见demo "multiple-datasource-seata"
#client配置注意点：
spring:
  application:
    name: multi-datasource-service  # 应用名

  datasource:
    # dynamic-datasource-spring-boot-starter 动态数据源的配配项，对应 DynamicDataSourceProperties 类
    dynamic:
      primary: order-ds # 设置默认的数据源或者数据源组，默认值即为 master
      datasource:
        # 订单 order 数据源配置
        order-ds:
          url: jdbc:mysql://127.0.0.1:3306/seata_order?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password: root
        # 账户 pay 数据源配置
        account-ds:
          url: jdbc:mysql://127.0.0.1:3306/seata_account?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password: root
        # 商品 product 数据源配置
        product-ds:
          url: jdbc:mysql://127.0.0.1:3306/seata_product?useSSL=false&useUnicode=true&characterEncoding=UTF-8
          driver-class-name: com.mysql.jdbc.Driver
          username: root
          password: root
      seata: true # 是否启动对 Seata 的集成

# Seata 配置项，对应 SeataProperties 类
seata:
  application-id: ${spring.application.name} # Seata 应用编号，默认为 ${spring.application.name}
  tx-service-group: ${spring.application.name}-group # Seata 事务组编号，用于 TC 集群名
  # 服务配置项，对应 ServiceProperties 类
  service:
    # 虚拟组和分组的映射
    vgroup-mapping:
      multi-datasource-service-group: default
    # 分组和 Seata 服务的映射
    grouplist:
      default: 127.0.0.1:8091

