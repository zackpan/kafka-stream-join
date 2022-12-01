package com.abn.demo.stream.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abn.demo.stream.model.Department;
import com.abn.demo.stream.model.Employee;
import com.abn.demo.stream.model.EmploymentHistory;
import com.abn.demo.stream.service.Producer;

@RestController
public class StreamController {
    @Autowired
    Producer producer;

    @PostMapping(value = "/publish")
    public ResponseEntity<String> publish() {
        try {
            return new ResponseEntity<>("Published", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/department")
    public void sendMessageToKafkaTopic(@RequestParam("id") Integer id, @RequestParam("name") String name) {
        Department dept = new Department(id,name);
 
        this.producer.publish(dept);
    }

    @PostMapping(value = "/employee")
    public void sendMessageToKafkaTopic(@RequestParam("id") Integer id, @RequestParam("name") String name, @RequestParam("dept_id") Integer deptId) {
        Employee emp = new Employee(id, deptId, name);
        this.producer.publish(emp);
    }

    @GetMapping(value = "/subscirbe")
    public ResponseEntity<String> subscribe() {
        try {
            return new ResponseEntity<>("Subscribed", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/loadData")
    public ResponseEntity<String> loadData() {
        try {
            this.producer.publish(new Department(10,"IT"));
            this.producer.publish(new Department(20,"HR"));
            this.producer.publish(new Department(30,"OPS"));
            this.producer.publish(new Employee(1, 10, "Zack"));
            this.producer.publish(new Employee(2, 20, "Tim"));
            this.producer.publish(new Employee(3, 30, "Lily"));
            this.producer.publish(new EmploymentHistory(100, 1, "ABN"));
            this.producer.publish(new EmploymentHistory(200, 1, "TD"));
            this.producer.publish(new EmploymentHistory(300, 2, "Google"));
            this.producer.publish(new EmploymentHistory(400, 3, "Microsoft"));
            return new ResponseEntity<>("Data Loaded", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
