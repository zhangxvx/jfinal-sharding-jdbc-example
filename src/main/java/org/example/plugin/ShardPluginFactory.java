package org.example.plugin;

import com.google.common.collect.Lists;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.druid.DruidPlugin;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.example.AppConfig;
import org.example.shard.PreciseModuloShardingDatabaseAlgorithm;
import org.example.shard.PreciseModuloShardingTableAlgorithm;
import org.example.shard.RangeModuloShardingDatabaseAlgorithm;
import org.example.shard.RangeModuloShardingTableAlgorithm;

import java.util.*;

public class ShardPluginFactory {
    public static Prop config = PropKit.use("config.properties");

    /**
     * 数据分片，单库分表
     */
    public static ShardDruidPlugin getShardTablePlugin() {
        DruidPlugin dp0 = new DruidPlugin(AppConfig.config.get("jdbc.master.url"), AppConfig.config.get("jdbc.master.user"), AppConfig.config.get("jdbc.master.password"));
        Map<String, DruidPlugin> druidPluginMap = new HashMap<>();
        druidPluginMap.put("ds0", dp0);

        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("t_order", "ds0.t_order_${0..1}");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);

        StandardShardingStrategyConfiguration strategyConfiguration = new StandardShardingStrategyConfiguration("order_id", new PreciseModuloShardingTableAlgorithm(), new RangeModuloShardingTableAlgorithm());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(strategyConfiguration);

        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        return new ShardDruidPlugin(shardingRuleConfig, druidPluginMap, props);
    }

    /**
     * 数据分片，分库分表
     */
    public static ShardDruidPlugin getShardDatabasePlugin() {
        DruidPlugin dp0 = new DruidPlugin(AppConfig.config.get("jdbc.master.url"), AppConfig.config.get("jdbc.master.user"), AppConfig.config.get("jdbc.master.password"));
        DruidPlugin dp1 = new DruidPlugin(AppConfig.config.get("jdbc.salve1.url"), AppConfig.config.get("jdbc.salve1.user"), AppConfig.config.get("jdbc.salve1.password"));
        Map<String, DruidPlugin> druidPluginMap = new HashMap<>();
        druidPluginMap.put("ds0", dp0);
        druidPluginMap.put("ds1", dp1);

        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("t_order", "ds${0..1}.t_order_${0..1}");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);

        StandardShardingStrategyConfiguration tableStrategyConfig = new StandardShardingStrategyConfiguration("order_id", new PreciseModuloShardingTableAlgorithm(), new RangeModuloShardingTableAlgorithm());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(tableStrategyConfig);

        StandardShardingStrategyConfiguration databaseStrategyConfig = new StandardShardingStrategyConfiguration("order_id", new PreciseModuloShardingDatabaseAlgorithm(), new RangeModuloShardingDatabaseAlgorithm());
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(databaseStrategyConfig);

        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        return new ShardDruidPlugin(shardingRuleConfig, druidPluginMap, props);
    }

    /**
     * 读写分离
     */
    public static MasterSlaveDruidPlugin getMasterSlavePlugin() {
        DruidPlugin dp0 = new DruidPlugin(AppConfig.config.get("jdbc.master.url"), AppConfig.config.get("jdbc.master.user"), AppConfig.config.get("jdbc.master.password"));
        DruidPlugin dp1 = new DruidPlugin(AppConfig.config.get("jdbc.salve1.url"), AppConfig.config.get("jdbc.salve1.user"), AppConfig.config.get("jdbc.salve1.password"));
        DruidPlugin dp2 = new DruidPlugin(AppConfig.config.get("jdbc.salve2.url"), AppConfig.config.get("jdbc.salve2.user"), AppConfig.config.get("jdbc.salve2.password"));
        Map<String, DruidPlugin> druidPluginMap = new HashMap<>();
        druidPluginMap.put("ds_master", dp0);
        druidPluginMap.put("ds_slave0", dp1);
        druidPluginMap.put("ds_slave1", dp2);

        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration("ds_master_slave", "ds_master", Arrays.asList("ds_slave0", "ds_slave1"));

        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        return new MasterSlaveDruidPlugin(masterSlaveRuleConfig, druidPluginMap, props);
    }

    /**
     * 读写分离+数据分片
     */
    public static ShardDruidPlugin getMasterSlaveShardTablePlugin() {
        DruidPlugin dp0 = new DruidPlugin(AppConfig.config.get("jdbc.master.url"), AppConfig.config.get("jdbc.master.user"), AppConfig.config.get("jdbc.master.password"));
        DruidPlugin dp1 = new DruidPlugin(AppConfig.config.get("jdbc.salve1.url"), AppConfig.config.get("jdbc.salve1.user"), AppConfig.config.get("jdbc.salve1.password"));
        DruidPlugin dp2 = new DruidPlugin(AppConfig.config.get("jdbc.salve2.url"), AppConfig.config.get("jdbc.salve2.user"), AppConfig.config.get("jdbc.salve2.password"));
        Map<String, DruidPlugin> druidPluginMap = new HashMap<>();
        druidPluginMap.put("ds_master", dp0);
        druidPluginMap.put("ds_slave0", dp1);
        druidPluginMap.put("ds_slave1", dp2);

        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration orderTableRuleConfiguration = new TableRuleConfiguration("t_order", "ds_master_slave.t_order_${0..1}");
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfiguration);

        StandardShardingStrategyConfiguration strategyConfiguration = new StandardShardingStrategyConfiguration("order_id", new PreciseModuloShardingTableAlgorithm(), new RangeModuloShardingTableAlgorithm());
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(strategyConfiguration);

        MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration("ds_master_slave", "ds_master", Arrays.asList("ds_slave0", "ds_slave1"));
        List<MasterSlaveRuleConfiguration> masterSlaveRuleConfigList = Lists.newArrayList(masterSlaveRuleConfig);
        shardingRuleConfig.setMasterSlaveRuleConfigs(masterSlaveRuleConfigList);

        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true");
        return new ShardDruidPlugin(shardingRuleConfig, druidPluginMap, props);
    }
}
