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
	
	public static String dataSourcesNames = "master,";//master, ����slave.datasource.names��ֵ  �Զ��Ÿ���
	
	@Autowired
    private Environment env;//��ȡ�Զ�������
	
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
		//��������Ĭ�ϵ�����Դ
		dynamicDataSource.setDefaultTargetDataSource(targetDataSources.get("master"));
//		DataSourceContextHolder.setDataSource(ContextConst.DataSourceType.FIRST.name());//����ֱ��д��һ��
		//���ö�����Դ   
		HashMap<Object, Object> dataSourceMap = new HashMap<>();
		for (String key : targetDataSources.keySet()) {
			dataSourceMap.put(key, targetDataSources.get(key));
		}
		dynamicDataSource.setTargetDataSources(dataSourceMap);
		return dynamicDataSource;
	}
	
	
	private DataSource buildMasterDataSource() throws Exception{
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, url);//���ݿ�url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, username);//�û���
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, password);//����
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, driverClassName);//Driver  ���ݿ�����

		return DruidDataSourceFactory.createDataSource(properties);
	}
	
	private HashMap<String, DataSource> buildSlaveDataSources() throws Exception{
		 // ��ȡ�����ļ���ȡ��������Դ��Ҳ����ͨ��defaultDataSource��ȡ���ݿ��ȡ��������Դ
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "slave.datasource.");
        String dsPrefixs = propertyResolver.getProperty("names");
        dataSourcesNames += dsPrefixs;
        LOGGER.info("dataSourcesNames: " + dataSourcesNames);
        HashMap<String, DataSource> slaveDataSources = new HashMap<>();
        for (String dsPrefix : dsPrefixs.split(",")) {// �������Դ
            Map<String, Object> dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
            DataSource slaveDataSource = buildDataSource(dsMap);
            slaveDataSources.put(dsPrefix, slaveDataSource);
        }
        return slaveDataSources;
	}
	
	private Properties setCustomProperties(){
		Properties properties = new Properties();
		properties.put(DruidDataSourceFactory.PROP_INITIALSIZE, initialSize);//��ʼ��ʱ�����������ӵĸ��� Ĭ����0  ����10
		properties.put(DruidDataSourceFactory.PROP_MAXACTIVE, maxActive);//������ӳ�����Ĭ����8  50
		properties.put(DruidDataSourceFactory.PROP_MINIDLE, minIdle);//��С���ӳ����� 10
		properties.put(DruidDataSourceFactory.PROP_MAXWAIT, maxWait);//��ȡ����ʱ���ȴ�ʱ��  ������������ֵ֮�� Ĭ��ʹ�ù�ƽ�� ����Ч�ʻ������½�  ����ͨ������userUnfairLock Ϊtrueʹ�÷ǹ�ƽ��
		properties.put(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, timeBetweenEvictionRunsMillis);//���������壺 1) Destroy�̻߳������ӵļ��ʱ��2) testWhileIdle���ж����ݣ���ϸ��testWhileIdle���Ե�˵��
		properties.put(DruidDataSourceFactory.PROP_MINEVICTABLEIDLETIMEMILLIS, minEvicatableIdleTimeMillis);
		properties.put(DruidDataSourceFactory.PROP_VALIDATIONQUERY, validationQuery);//������������Ƿ���Ч��sql��Ҫ����һ����ѯ���  ����˴�Ϊnull ������������������
		properties.put(DruidDataSourceFactory.PROP_TESTWHILEIDLE, testWhileIdle);
		properties.put(DruidDataSourceFactory.PROP_TESTONBORROW, testOnBorrow);
		properties.put(DruidDataSourceFactory.PROP_REMOVEABANDONED, removeAbandoned);
		properties.put(DruidDataSourceFactory.PROP_REMOVEABANDONEDTIMEOUT, removeAbandonedTimeOut);
		properties.put(DruidDataSourceFactory.PROP_TESTONRETURN, testOnReturn);
		return properties;
	}
	
	public DataSource buildDataSource(Map<String, Object> dsMap) throws Exception {
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, dsMap.get("url").toString());//���ݿ�url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, dsMap.get("username").toString());//�û���
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, dsMap.get("password").toString());//����
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, dsMap.get("driverClassName").toString());//Driver  ���ݿ�����

		return DruidDataSourceFactory.createDataSource(properties);
	}
	
}
