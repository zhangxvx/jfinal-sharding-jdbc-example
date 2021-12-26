# Jfinal使用Sharding-JDBC学习笔记

***

## 参考

`https://shardingsphere.apache.org/document/4.1.1/cn/overview/`
<br>
`https://github.com/apache/shardingsphere/tree/4.1.1/examples`

## 引入依赖

```xml

<dependency>
    <groupId>com.jfinal</groupId>
    <artifactId>jfinal</artifactId>
    <version>4.9.15</version>
</dependency>
<dependency>
<groupId>org.apache.shardingsphere</groupId>
<artifactId>sharding-jdbc-core</artifactId>
<version>4.1.1</version>
</dependency>
```

***

# 配置示例

## 创建库和表

```sql
-- 创建库
create database ds_0;
create database ds_1;
create database ds_2;

-- 每个库创建表
CREATE TABLE `t_order` (
  `order_id` int NOT NULL,
  `order_name` varchar(100) NOT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB;

CREATE TABLE `t_order_0` (
 `order_id` int NOT NULL,
 `order_name` varchar(100) NOT NULL,
 PRIMARY KEY (`order_id`)
) ENGINE=InnoDB;

CREATE TABLE `t_order_1` (
 `order_id` int NOT NULL,
 `order_name` varchar(100) NOT NULL,
 PRIMARY KEY (`order_id`)
) ENGINE=InnoDB;
-- 测试数据
INSERT INTO `t_order`(`order_id`, `order_name`) VALUES (1, 'name1');
INSERT INTO `t_order`(`order_id`, `order_name`) VALUES (2, 'name2');
INSERT INTO `t_order_1`(`order_id`, `order_name`) VALUES (1, 'name1');
INSERT INTO `t_order_0`(`order_id`, `order_name`) VALUES (2, 'name2');
```

## Java配置

### 自定义Druid插件

```java
//数据分片——单库分表
ShardDruidPlugin shardDruidPlugin=ShardPluginFactory.getShardTablePlugin();
//数据分片——分库分表
        ShardDruidPlugin shardDruidPlugin=ShardPluginFactory.getShardDatabasePlugin();
//读写分离
        MasterSlaveDruidPlugin shardDruidPlugin=ShardPluginFactory.getMasterSlavePlugin();
//读写分离+数据分片
        ShardDruidPlugin shardDruidPlugin=ShardPluginFactory.getMasterSlaveShardTablePlugin();
```

### 配置插件

```java
public void configPlugin(Plugins me) {
    //数据分片——单库分表
    ShardDruidPlugin shardDruidPlugin = ShardPluginFactory.getShardTablePlugin();
    
    ActiveRecordPlugin arp = new ActiveRecordPlugin(shardDruidPlugin);
    _MappingKit.mapping(arp);
    arp.setDevMode(true);
    arp.setShowSql(true);
    me.add(arp);
}
```

### Sharding-JDBC配置

```java
/**
 * 数据分片，单库分表
 */
public static ShardDruidPlugin getShardTablePlugin(){
        DruidPlugin dp0=new DruidPlugin(AppConfig.config.get("jdbc.master.url"),AppConfig.config.get("jdbc.master.user"),AppConfig.config.get("jdbc.master.password"));
        Map<String, DruidPlugin> druidPluginMap=new HashMap<>();
        druidPluginMap.put("ds0",dp0);

        ShardingRuleConfiguration shardingRuleConfig=new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig=new TableRuleConfiguration("t_order","ds0.t_order_${0..1}");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);

        StandardShardingStrategyConfiguration strategyConfiguration=new StandardShardingStrategyConfiguration("order_id",new PreciseModuloShardingTableAlgorithm(),new RangeModuloShardingTableAlgorithm());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(strategyConfiguration);

        Properties props=new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(),"true");
        return new ShardDruidPlugin(shardingRuleConfig,druidPluginMap,props);
        }
```

***

# 知识

# 一、数据分片

## 概念

### 分片键

用于分片的数据库字段，是将数据库(表)水平拆分的关键字段。例：将订单表中的订单主键的尾数取模分片，则订单主键为分片字段。

### 分片策略

真正可用于分片操作的是分片键 + 分片算法，也就是分片策略

#### 标准分片策略

- 对应StandardShardingStrategy。提供对SQL语句中的=, >, <, >=, <=, IN和BETWEEN AND的分片操作支持。
- StandardShardingStrategy只支持单分片键，提供PreciseShardingAlgorithm和RangeShardingAlgorithm两个分片算法。
- PreciseShardingAlgorithm是必选的，用于处理=和IN的分片。
- RangeShardingAlgorithm是可选的，用于处理BETWEEN AND, >, <, >=, <=分片，如果不配置RangeShardingAlgorithm，SQL中的BETWEEN AND将按照全库路由处理。

#### 复合分片策略

对应ComplexShardingStrategy。

- 复合分片策略。提供对SQL语句中的=, >, <, >=, <=, IN和BETWEEN AND的分片操作支持。
- ComplexShardingStrategy支持多分片键，由于多分片键之间的关系复杂，因此并未进行过多的封装，而是直接将分片键值组合以及分片操作符透传至分片算法，完全由应用开发者实现，提供最大的灵活度。

#### 行表达式分片策略

- 对应InlineShardingStrategy。使用Groovy的表达式，提供对SQL语句中的=和IN的分片操作支持，只支持单分片键。
- 对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的Java代码开发，如: t_user_$->{u_id % 8} 表示t_user表根据u_id模8，而分成8张表，表名称为t_user_0到t_user_7。

#### Hint分片策略

- 对应HintShardingStrategy。通过Hint指定分片值而非从SQL中提取分片值的方式进行分片的策略。

#### 不分片策略

- 对应NoneShardingStrategy。不分片的策略。

## 配置

### 分片规则

分片规则配置的总入口。包含数据源配置、表配置、绑定表配置以及读写分离配置等。

### 数据源配置

真实数据源列表。

### 表配置

逻辑表名称、数据节点与分表规则的配置。

### 数据节点配置

用于配置逻辑表与真实表的映射关系。可分为均匀分布和自定义分布两种形式。

### 分片策略配置

对于分片策略存有数据源分片策略和表分片策略两种维度。

- 数据源分片策略<br>
  对应于DatabaseShardingStrategy。用于配置数据被分配的目标数据源。
- 表分片策略<br>
  对应于TableShardingStrategy。用于配置数据被分配的目标表，该目标表存在与该数据的目标数据源内。故表分片策略是依赖与数据源分片策略的结果的。 两种策略的API完全相同。

### 自增主键生成策略

通过在客户端生成自增主键替换以数据库原生自增主键的方式，做到分布式主键无重复。

# 二、读写分离

## 概念

### 主库

添加、更新以及删除数据操作所使用的数据库，目前仅支持单主库。

### 从库

查询数据操作所使用的数据库，可支持多从库。

### 主从同步

将主库的数据异步的同步到从库的操作。由于主从同步的异步性，从库与主库的数据会短时间内不一致。

### 负载均衡策略

通过负载均衡策略将查询请求疏导至不同从库。