package com.abn.demo.stream;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
@EnableKafka
public class StreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamApplication.class, args);
	}
	@Bean
    NewTopic deptartmentTopic() {
        return TopicBuilder.name("DEPT").partitions(1).replicas(1).build();
    }

	@Bean
    NewTopic employeeTopic() {
        return TopicBuilder.name("EMPLOYEE").partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic employeeHistoryTopic() {
        return TopicBuilder.name("EMPLOYMENT-HISTORY").partitions(1).replicas(1).build();
    }
}
