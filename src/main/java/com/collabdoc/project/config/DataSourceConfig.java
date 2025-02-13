package com.collabdoc.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
// import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${db.type}")
    private String dbType;

    @Value("${spring.datasource.url:#{null}}")
    private String sqlUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String sqlUsername;

    @Value("${spring.datasource.password:#{null}}")
    private String sqlPassword;

    @Value("${spring.datasource.driver-class-name:#{null}}")
    private String driverClassName;

    /** ✅ **Use relational Database from Configuration** */
    @Bean
    @ConditionalOnProperty(name = "db.type", havingValue = "sql")
    public DataSource relationalDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(sqlUrl);
        dataSource.setUsername(sqlUsername);
        dataSource.setPassword(sqlPassword);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }
}
