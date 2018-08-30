package com.yj.multids.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;


@Configuration
public class DataSourceCustomConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceCustomConfig.class);
	
	public static String dataSourcesNames = "master,";//master, 加上slave.datasource.names的值  以逗号隔开
	
	@Autowired
    private Environment env;//获取自定义配置
	
	@Value("${spring.datasource.druid.url}")
	private String url;

	@Value("${spring.datasource.druid.username}")
	private String username;

	@Value("${spring.datasource.druid.driver-class-name}")
	private String driverClassName;

	@Value("${spring.datasource.druid.password}")
	private String password;
	
	@Value("${spring.datasource.druid.initial-size}")
	private String initialSize;

	@Value("${spring.datasource.druid.max-active}")
	private String maxActive;

	@Value("${spring.datasource.druid.min-idle}")
	private String minIdle;

	@Value("${spring.datasource.druid.max-wait}")
	private String maxWait;

	@Value("${spring.datasource.druid.time-between-eviction-runs-millis}")
	private String timeBetweenEvictionRunsMillis;

	@Value("${spring.datasource.druid.min-evictable-idle-time-millis}")
	private String minEvicatableIdleTimeMillis;

	@Value("${spring.datasource.druid.validation-query}")
	private String validationQuery;

	@Value("${spring.datasource.druid.test-while-idle}")
	private String testWhileIdle;
	
	@Value("${spring.datasource.druid.test-on-borrow}")
	private String testOnBorrow;
	
	@Value("${spring.datasource.druid.test-on-return}")
	private String testOnReturn;
	
	@Value("${spring.datasource.druid.remove-abandoned}")
	private String removeAbandoned;
	
	@Value("${spring.datasource.druid.remove-abandoned-timeout-millis}")
	private String removeAbandonedTimeOut;
	
	
	@Bean("targetDataSources")
	public HashMap<String, DataSource>  buildTargetDataSources() throws Exception{
		HashMap<String, DataSource> targetDataSources = new HashMap<>();
		DataSource masterDS = buildMasterDataSource();
		((DruidDataSource)masterDS).setBreakAfterAcquireFailure(true);
		targetDataSources.put("master", masterDS);
		targetDataSources.putAll(buildSlaveDataSources());
		return targetDataSources;
	}
	
	@Primary
	@Bean(name="dynamicDataSource")
	public DataSource dynamicDataSource() throws Exception{
		DynamicDataSource dynamicDataSource = new DynamicDataSource();
		HashMap<String, DataSource> targetDataSources = buildTargetDataSources();
		//首先配置默认的数据源
		dynamicDataSource.setDefaultTargetDataSource(targetDataSources.get("master"));
//		DataSourceContextHolder.setDataSource(ContextConst.DataSourceType.FIRST.name());//首先直接写第一个
		//配置多数据源   
		HashMap<Object, Object> dataSourceMap = new HashMap<>();
		for (String key : targetDataSources.keySet()) {
			dataSourceMap.put(key, targetDataSources.get(key));
		}
		dynamicDataSource.setTargetDataSources(dataSourceMap);
		return dynamicDataSource;
	}
	
	
	private DataSource buildMasterDataSource() throws Exception{
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, url);//数据库url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, username);//用户名
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, password);//密码
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, driverClassName);//Driver  数据库驱动

		return DruidDataSourceFactory.createDataSource(properties);
	}
	
	private HashMap<String, DataSource> buildSlaveDataSources() throws Exception{
		 // 读取配置文件获取更多数据源，也可以通过defaultDataSource读取数据库获取更多数据源
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "slave.datasource.");
        String dsPrefixs = propertyResolver.getProperty("names");
        dataSourcesNames += dsPrefixs;
        LOGGER.info("dataSourcesNames: " + dataSourcesNames);
        HashMap<String, DataSource> slaveDataSources = new HashMap<>();
        for (String dsPrefix : dsPrefixs.split(",")) {// 多个数据源
            Map<String, Object> dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
            DataSource slaveDataSource = buildDataSource(dsMap);
            slaveDataSources.put(dsPrefix, slaveDataSource);
        }
        return slaveDataSources;
	}
	
	private Properties setCustomProperties(){
		Properties properties = new Properties();
		properties.put(DruidDataSourceFactory.PROP_INITIALSIZE, initialSize);//初始化时建立物理连接的个数 默认是0  配置10
		properties.put(DruidDataSourceFactory.PROP_MAXACTIVE, maxActive);//最大连接池数量默认是8  50
		properties.put(DruidDataSourceFactory.PROP_MINIDLE, minIdle);//最小连接池数量 10
		properties.put(DruidDataSourceFactory.PROP_MAXWAIT, maxWait);//获取连接时最大等待时间  如果配置了这个值之后 默认使用公平锁 并发效率会有所下降  可以通过配置userUnfairLock 为true使用非公平锁
		properties.put(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, timeBetweenEvictionRunsMillis);//有两个含义： 1) Destroy线程会检测连接的间隔时间2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
		properties.put(DruidDataSourceFactory.PROP_MINEVICTABLEIDLETIMEMILLIS, minEvicatableIdleTimeMillis);
		properties.put(DruidDataSourceFactory.PROP_VALIDATIONQUERY, validationQuery);//用来检测连接是否有效的sql，要求是一个查询语句  如果此处为null 后面三个不会起作用
		properties.put(DruidDataSourceFactory.PROP_TESTWHILEIDLE, testWhileIdle);
		properties.put(DruidDataSourceFactory.PROP_TESTONBORROW, testOnBorrow);
		properties.put(DruidDataSourceFactory.PROP_REMOVEABANDONED, removeAbandoned);
		properties.put(DruidDataSourceFactory.PROP_REMOVEABANDONEDTIMEOUT, removeAbandonedTimeOut);
		properties.put(DruidDataSourceFactory.PROP_TESTONRETURN, testOnReturn);
		return properties;
	}
	
	public DataSource buildDataSource(Map<String, Object> dsMap) throws Exception {
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, dsMap.get("url").toString());//数据库url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, dsMap.get("username").toString());//用户名
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, dsMap.get("password").toString());//密码
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, dsMap.get("driverClassName").toString());//Driver  数据库驱动

		return DruidDataSourceFactory.createDataSource(properties);
	}
	
}
