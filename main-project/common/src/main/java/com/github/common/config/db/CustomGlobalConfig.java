package com.github.common.config.db;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.github.common.db.mapper.common.CustomerSqlInjector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
@Configuration
public class CustomGlobalConfig {
    @Bean(name = "globalConfiguration")
    public GlobalConfig globalConfiguration() {
        GlobalConfig globalConfig = new GlobalConfig();
        //这个Bean主要是将自定义的CustomerSqlInjector配置仅GlobalConfig中，使自定义的mybatis plus通用方法生效
        globalConfig.setSqlInjector(new CustomerSqlInjector());
        return globalConfig;
    }
}
