package com.collabdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = { 
        MongoRepositoriesAutoConfiguration.class, 
        JpaRepositoriesAutoConfiguration.class, 
        HibernateJpaAutoConfiguration.class
})
public class CollabDocApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollabDocApplication.class, args);
	}

}
