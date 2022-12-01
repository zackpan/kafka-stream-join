package com.abn.demo.stream.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentHistory {
    Integer id;
    Integer empId;
    String employerName;
}
