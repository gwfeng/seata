  需要注意的是：本工程seata 采用的是手动配置的方式,
  resource 目录下需要配置file.conf 以及 register.conf 文件
  手动配置对应的依赖如下：，自动配置见另外的demo
<dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
            <scope>compile</scope>
            <exclusions>
                <exclusion>
                    <groupId>io.seata</groupId>
                    <artifactId>seata-all</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>io.seata</groupId>
            <artifactId>seata-all</artifactId>
            <version>${seata.version}</version>
        </dependency>