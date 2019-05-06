package com.paladin.cas;

import javax.sql.DataSource;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * 在spring.factories文件中注入
 * @author TontoZhou
 * @since 2019年4月25日
 */
@Configuration("CustomAuthConfig")
@EnableConfigurationProperties(CustomAuthProperties.class)
public class CustomAuthConfig implements AuthenticationEventExecutionPlanConfigurer {

	@Autowired
	private CustomAuthProperties customAuthProperties;
	
	@Autowired
	@Qualifier("servicesManager")
	private ServicesManager servicesManager;

	@Bean
	public AuthenticationHandler myAuthenticationHandler() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl(customAuthProperties.getDbUrl());
		dataSource.setPassword(customAuthProperties.getDbPassword());
		dataSource.setUsername(customAuthProperties.getDbUsername());
		dataSource.setName(customAuthProperties.getDbName());
		dataSource.setMaxWait(10000);
		final PaladinAuthenticationHandler handler = new PaladinAuthenticationHandler(PaladinAuthenticationHandler.class.getSimpleName(), servicesManager,
				new DefaultPrincipalFactory(), 1, dataSource);
		return handler;
	}

	@Override
	public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
		plan.registerAuthenticationHandler(myAuthenticationHandler());
	}
}
