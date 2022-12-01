package com.abn.demo.stream.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.abn.demo.stream.model.*;

import lombok.extern.apachecommons.CommonsLog;

@Service
@CommonsLog(topic = "Consumer Logger")
public class Consumer {
    @Value("${topic.department}")
    private String topic;
  
    @Value("${spring.kafka.consumer.group-id}")
    private String group_id;
  
    
    @KafkaListener(topics = "#{'${topic.department}'}", groupId = "#'${spring.kafka.consumer.group-id}'")
    //@KafkaListener(topics = "${topic.department}", groupId = "${spring.kafka.consumer.group-id}")
    public void onDepartmentUpdate(ConsumerRecord<Integer, Department> record) {
        log.info(String.format("Consumed message -> %s", record.value()));      
    } 
    
    @KafkaListener(topics = "#{'${topic.employee}'}", groupId = "#'${spring.kafka.consumer.group-id}'")
    //@KafkaListener(topics = "${topic.employee}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEmployeeUpdate(ConsumerRecord<Integer, Employee> record) {
        log.info(String.format("Consumed message -> %s", record.value()));      
    } 

    @KafkaListener(topics = "#{'${topic.employment-history}'}", groupId = "#'${spring.kafka.consumer.group-id}'")
    //@KafkaListener(topics = "${topic.employee}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEmploymentUpdate(ConsumerRecord<Integer, EmploymentHistory> record) {
        log.info(String.format("Consumed message -> %s", record.value()));      
    } 
    
    @KafkaListener(topics = "#{'${topic.employee-result}'}", groupId = "#'${spring.kafka.consumer.group-id}'")
    public void onEmployeeResultUpdate(ConsumerRecord<Integer, EmployeeResult> record) {
        log.info(String.format("Consumed message -> %s", record.value()));      
    }               
}
