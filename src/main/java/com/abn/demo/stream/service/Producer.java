package com.abn.demo.stream.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.abn.demo.stream.model.*;

@Service
public class Producer {
    private KafkaTemplate<Integer, Department> deptTemplate;
    private String deptTopic = "DEPT";

    private KafkaTemplate<Integer, Employee> empTemplate;
    private String empTopic = "EMPLOYEE";

    private KafkaTemplate<Integer, EmploymentHistory> historyTemplate;
    private String historyTopic = "EMPLOYMENT-HISTORY";

    @Autowired
    public Producer(KafkaTemplate<Integer, Department> deptTemplate, 
        KafkaTemplate<Integer, Employee> empTemplate,
        KafkaTemplate<Integer, EmploymentHistory> historyTemplate) {
        this.deptTemplate = deptTemplate;
        this.empTemplate = empTemplate;
        this.historyTemplate = historyTemplate;
    }

    public void publish(Department dept) {
        try {
            this.deptTemplate.send(this.deptTopic, dept.getDeptId(), dept);
        } catch (Exception ex) {
            System.out.print(ex);
        }

    }
    public void publish(Employee emp) {
        try {
            this.empTemplate.send(this.empTopic, emp.getEmpId(), emp);
        } catch (Exception ex) {
            System.out.print(ex);
        }

    }  
    public void publish(EmploymentHistory history) {
        try {
            this.historyTemplate.send(this.historyTopic, history.getId(), history);
        } catch (Exception ex) {
            System.out.print(ex);
        }

    }      
}
