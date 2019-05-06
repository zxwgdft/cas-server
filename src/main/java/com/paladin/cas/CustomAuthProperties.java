package com.paladin.cas;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConditionalOnMissingBean
@ConfigurationProperties(value = "paladin")
public class CustomAuthProperties {
	
	private String dbUrl = "jdbc:mysql://127.0.0.1:3306/credit?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false";
	private String dbName = "credit";
	private String dbUsername = "root";
	private String dbPassword = "";
	
	
	public String getDbUrl() {
		return dbUrl;
	}
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getDbPassword() {
		return dbPassword;
	}
	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}
	public String getDbUsername() {
		return dbUsername;
	}
	public void setDbUsername(String dbUsername) {
		this.dbUsername = dbUsername;
	}
	
	
}
