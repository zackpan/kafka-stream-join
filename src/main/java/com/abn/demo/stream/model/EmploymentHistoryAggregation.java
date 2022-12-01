package com.abn.demo.stream.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class EmploymentHistoryAggregation {
    Integer id;
    Integer empId;
    List<String> employmentHistory = new ArrayList<String>();

    public void add(String employerName)
    {
        this.employmentHistory.add(employerName);
    }
}
