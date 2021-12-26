package org.example;

import com.jfinal.config.*;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import org.example.model.Order;
import org.example.model._MappingKit;
import org.example.plugin.ShardDruidPlugin;
import org.example.plugin.ShardPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class AppConfig extends JFinalConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    public static Prop config;

    static void loadConfig() {
        if (config == null) {
            config = PropKit.use("config.properties");
        }
    }

    public static DruidPlugin createDruidPlugin() {
        loadConfig();

        return new DruidPlugin(config.get("jdbc.source.url"), config.get("jdbc.source.user"), config.get("jdbc.source.password"));
    }

    /**
     * 配置常量
     */
    public void configConstant(Constants me) {
        loadConfig();
        me.setDevMode(config.getBoolean("devMode", false));
    }

    /**
     * 配置路由
     */
    public void configRoute(Routes me) {
        me.scan("org.example");
    }

    public void configEngine(Engine me) {
    }

    /**
     * 配置插件
     */
    public void configPlugin(Plugins me) {
        //数据分片——单库分表
        ShardDruidPlugin shardDruidPlugin = ShardPluginFactory.getShardTablePlugin();
        // //数据分片——分库分表
        // ShardDruidPlugin shardDruidPlugin = ShardPluginFactory.getShardDatabasePlugin();
        // //读写分离
        // MasterSlaveDruidPlugin shardDruidPlugin = ShardPluginFactory.getMasterSlavePlugin();
        // //读写分离+数据分片
        // ShardDruidPlugin shardDruidPlugin = ShardPluginFactory.getMasterSlaveShardTablePlugin();
        me.add(shardDruidPlugin);

        ActiveRecordPlugin arp = new ActiveRecordPlugin(shardDruidPlugin);
        _MappingKit.mapping(arp);
        arp.setDevMode(true);
        arp.setShowSql(true);
        me.add(arp);
    }

    /**
     * 配置全局拦截器
     */
    public void configInterceptor(Interceptors me) {
    }

    /**
     * 配置处理器
     */
    public void configHandler(Handlers me) {
    }

    @Override
    public void onStart() {
        List<Order> orders1 = new Order().dao().find("SELECT *  FROM t_order WHERE order_id=1 order by order_id asc");
        log.error("orders1 = " + orders1);
        List<Order> orders2 = new Order().dao().find("SELECT *  FROM t_order WHERE order_id in (1,2)  order by order_id asc");
        log.error("orders2 = " + orders2);

        List<Order> orders3 = new Order().dao().find("SELECT *  FROM t_order WHERE order_id between 0 and 5 order by order_id asc");
        log.error("orders3 = " + orders3);

        List<Order> orders4 = new Order().dao().find("SELECT *  FROM t_order order by order_id asc");
        log.error("orders4 = " + orders4);

        // int i = Db.update("insert into `t_order`(order_id,order_name) values (4,'name4')");
        // log.error("save = " + i);
    }
}
