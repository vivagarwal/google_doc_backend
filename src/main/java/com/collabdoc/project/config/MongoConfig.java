package com.collabdoc.project.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.collabdoc.project.repository.mongo")
@ConditionalOnProperty(name = "db.type", havingValue = "mongodb")
public class MongoConfig {
}
