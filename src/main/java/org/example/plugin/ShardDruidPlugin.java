package org.example.plugin;

import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.IDataSourceProvider;
import com.jfinal.plugin.druid.DruidPlugin;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ShardDruidPlugin implements IPlugin, IDataSourceProvider {
    //分表分库的rule
    ShardingRuleConfiguration shardingRuleConfig;
    //数据源map
    Map<String, DruidPlugin> druidPlugins;
    //原数据库连接源map
    Map<String, DataSource> dataSourceMap;
    //最终sharding-jdbc封装后的数据库连接源
    DataSource dataSource;
    //配置
    Properties props;

    public ShardDruidPlugin(ShardingRuleConfiguration shardingRuleConfig, Map<String, DruidPlugin> druidPlugins, Properties props) {
        this.shardingRuleConfig = shardingRuleConfig;
        this.druidPlugins = druidPlugins;
        this.dataSourceMap = new HashMap<>();
        this.props = props;
    }

    public boolean start() {
        //遍历数据源 ，将数据源加入sharding jdbc
        for (Map.Entry<String, DruidPlugin> entry : druidPlugins.entrySet()) {
            entry.getValue().start();
            dataSourceMap.put(entry.getKey(), entry.getValue().getDataSource());
        }
        try {
            //获得数据库连接类
            dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, props);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean stop() {
        for (Map.Entry<String, DruidPlugin> entry : druidPlugins.entrySet()) {
            entry.getValue().stop();
            dataSourceMap.put(entry.getKey(), entry.getValue().getDataSource());
        }
        return true;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}