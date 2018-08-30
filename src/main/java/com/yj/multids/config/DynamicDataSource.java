package com.yj.multids.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;


/**
 * Desc :  鍔ㄦ�佹暟鎹簮
 * User : RICK
 * Time : 2017/8/28 10:36
  */
public class DynamicDataSource extends AbstractRoutingDataSource{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSource.class);
	
	private volatile String resolvedDataSourceName = "master";
	
    @Override
    protected Object determineCurrentLookupKey() {
        return null;
    }
//    
	@Autowired()
	@Qualifier("targetDataSources")
	private HashMap<String, DataSource> aliveDataSources;
	
    @Override
    public Connection getConnection() throws SQLException{
    	LOGGER.info("connection.......");
    	//在这里去判断  是否连接上 然后 去监控 若没有连接上 就切换数据源
    	Connection connection = null;
    	boolean isOK = false;
    	while(!isOK){
    		try {
    			connection = super.getConnection();
    			LOGGER.info("connected.11111............." + connection);
    			//即使获取到连接 有可能是从线程池拿出来用的 不一定有效
    			if(connection != null && !connection.isValid(2)){
    				LOGGER.info("DataSource unusing........");
        	    	throw new Exception();
    			}
    			isOK = true;
    		}catch (Exception e) {
    			String dataSourceName = "";
    			int nameLength = aliveDataSources.size() - 1; //要减去master  就是剩下的备份机个数
    			if(resolvedDataSourceName.equals("master")){
    				if(nameLength < 1){
    					e.printStackTrace();//没有备份的直接报错
    				}
    				dataSourceName = "slave1";
    			}else{
    				int nameCount = Integer.parseInt(String.valueOf(resolvedDataSourceName.charAt(5)));
    				
    				if(++nameCount > nameLength){
    					dataSourceName = "master";//又回到master
    				}else{
    					dataSourceName = "slave" + nameCount;
    				}
    			}
    			LOGGER.info("change datasource " + dataSourceName);
    			resolvedDataSourceName = dataSourceName;
    			super.setDefaultTargetDataSource(aliveDataSources.get(dataSourceName));//修改默认连接的数据库
    	    	super.afterPropertiesSet();
    		}
    	}
    	return  connection;
    	
    }
    
}
