package com.collabdoc.project.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.collabdoc.project.repository.sql")
@ConditionalOnProperty(name = "db.type", havingValue = "sql")
public class JpaConfig {
}

