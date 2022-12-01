package com.abn.demo.stream.model;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class EmployeeResult {
    Integer deptId;
    Integer empId;
    String deptName;
    String empName;
    List<String> employmentHistory;
}
