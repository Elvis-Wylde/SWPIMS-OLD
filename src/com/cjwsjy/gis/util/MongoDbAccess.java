package com.cjwsjy.gis.util;

import java.util.HashMap;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class MongoDbAccess {
	// private final static String HOST = "10.7.8.139";// IP  
	private final static String HOST = "10.7.3.251";// IP  
    private final static int PORT = 27017;// 端口  
    private static MongoClient mongoClient = null;
    private static HashMap<String, MongoDatabase> mongoDbMap = new HashMap<String, MongoDatabase>();
  
    public static MongoDatabase getDatabase(String dbName) {
    	if (mongoClient == null) {
    		initMongoClient();
    	}
    	if (mongoDbMap.containsKey(dbName)) {
    		return mongoDbMap.get(dbName);
    	} else {
    		MongoDatabase db = mongoClient.getDatabase(dbName);
    		mongoDbMap.put(dbName, db);
    		return db;
    	}
    }
  
    /** 
     * 初始化连接池 
     */
    private static void initMongoClient() {
    	MongoClientOptions.Builder opts = new MongoClientOptions.Builder();
    	opts.connectionsPerHost(10);// 与目标数据库可以建立的最大链接数
    	opts.connectTimeout(1000 * 60 * 20);// 与数据库建立链接的超时时间
    	opts.maxWaitTime(100 * 60 * 5);// 一个线程成功获取到一个可用数据库之前的最大等待时间
    	opts.threadsAllowedToBlockForConnectionMultiplier(100);
    	opts.maxConnectionIdleTime(0);
    	opts.maxConnectionLifeTime(0);
    	opts.socketTimeout(0);
    	opts.socketKeepAlive(true);
        MongoClientOptions myOptions = opts.build();
        mongoClient = new MongoClient(new ServerAddress(HOST, PORT), myOptions);
    }
    
}
