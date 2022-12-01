package com.abn.demo.stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import com.abn.demo.stream.model.*;

@Component
@EnableKafkaStreams
public class StreamProcessor {
      @Value("${spring.kafka.properties.schema.registry.url}")
      private String schemaRegistry;

      @Autowired
      public void process(StreamsBuilder builder) {
            final Map<String, String> configs = Collections.singletonMap("schema.registry.url", schemaRegistry);
            final Serde<String> stringSerde = Serdes.String();
            stringSerde.configure(configs, true);

            JsonSerializer<Department> deptSerializer = new JsonSerializer<>();
            JsonDeserializer<Department> deptDeserializer = new JsonDeserializer<>(Department.class);
            Serde<Department> deptSerde = Serdes.serdeFrom(deptSerializer, deptDeserializer);
            deptSerde.configure(configs, false);
            // select key then convert stream into table
            final KTable<Integer, Department> deptTable = builder.stream("DEPT",
                        Consumed.with(Serdes.Integer(), deptSerde))
                        .map((key, value) -> new KeyValue<>(value.getDeptId(), value))
                        .toTable(Materialized.<Integer, Department, KeyValueStore<Bytes, byte[]>>as("DEPT-MV")
                                    .withKeySerde(Serdes.Integer())
                                    .withValueSerde(deptSerde));
            JsonSerializer<Employee> empSerializer = new JsonSerializer<>();
            JsonDeserializer<Employee> empDeserializer = new JsonDeserializer<>(Employee.class);
            Serde<Employee> empSerde = Serdes.serdeFrom(empSerializer, empDeserializer);
            empSerde.configure(configs, false);
            // select key then convert stream into table
            final KTable<Integer, Employee> empTable = builder.stream("EMPLOYEE",
                        Consumed.with(Serdes.Integer(), empSerde))
                        .map((key, value) -> new KeyValue<>(value.getEmpId(), value))
                        .toTable(Materialized.<Integer, Employee, KeyValueStore<Bytes, byte[]>>as("EMPLOYEE-MV")
                                    .withKeySerde(Serdes.Integer())
                                    .withValueSerde(empSerde));

            JsonSerializer<EmployeeResult> employeeResultSerializer = new JsonSerializer<>();
            JsonDeserializer<EmployeeResult> employeeResultDeserializer = new JsonDeserializer<>(EmployeeResult.class);
            Serde<EmployeeResult> employeeResultSerde = Serdes.serdeFrom(employeeResultSerializer,
                        employeeResultDeserializer);
            empSerde.configure(configs, false);
            // N:1 join -> EMPLOYEE and DEPARTMENT
            final KTable<Integer, EmployeeResult> empDeptTable = empTable.join(deptTable,
                        // foreignKeyExtractor. Get dept_id from employee and join with dept
                        Employee::getDeptId,
                        // join emp and dept, return EmployeeResult
                        (emp, dept) -> {
                              EmployeeResult employeeResult = new EmployeeResult();
                              employeeResult.setDeptId(dept.getDeptId());
                              employeeResult.setDeptName(dept.getDeptName());
                              employeeResult.setEmpId(emp.getEmpId());
                              employeeResult.setEmpName(emp.getEmpName());
                              return employeeResult;
                        },
                        // store into materialized view with neam EMP-DEPT-MV
                        Materialized.<Integer, EmployeeResult, KeyValueStore<Bytes, byte[]>>as("EMP-DEPT-MV")
                                    .withKeySerde(Serdes.Integer())
                                    .withValueSerde(employeeResultSerde));
            /*
             * empDeptTable.toStream()
             * .map((key, value) -> new KeyValue<>(value.getEmpId(), value))
             * .peek((key,value) -> System.out.println("(empDeptTable) key,value = " + key +
             * "," + value))
             * .to("EMPLOYEE-DEPT", Produced.with(Serdes.Integer(), employeeResultSerde));
             */

            JsonSerializer<EmploymentHistory> employmentHistorySerializer = new JsonSerializer<>();
            JsonDeserializer<EmploymentHistory> employmentHistroyDeserializer = new JsonDeserializer<>(
                        EmploymentHistory.class);
            Serde<EmploymentHistory> employmentHistorySerde = Serdes.serdeFrom(employmentHistorySerializer,
                        employmentHistroyDeserializer);

            JsonSerializer<EmploymentHistoryAggregation> employmentHistoryAggregationSerializer = new JsonSerializer<>();
            JsonDeserializer<EmploymentHistoryAggregation> employmentHistroyAggregationDeserializer = new JsonDeserializer<>(
                        EmploymentHistoryAggregation.class);
            Serde<EmploymentHistoryAggregation> employmentHistoryAggregationSerde = Serdes.serdeFrom(
                        employmentHistoryAggregationSerializer,
                        employmentHistroyAggregationDeserializer);
            employmentHistorySerde.configure(configs, false);
            // a. select emp_id as key, group by key (emp_id) then aggregate the result
            final KTable<Integer, EmploymentHistoryAggregation> employmentHistoryAggr = builder
                        .stream("EMPLOYMENT-HISTORY",
                                    Consumed.with(Serdes.Integer(), employmentHistorySerde))
                        .selectKey((key, empHist) -> empHist.getEmpId())
                        .groupByKey(Grouped.with(Serdes.Integer(), employmentHistorySerde))
                        .aggregate(
                                    // Initialized Aggregator
                                    EmploymentHistoryAggregation::new,
                                    // Aggregate
                                    (empId, empHist, empHistAggr) -> {
                                          empHistAggr.setEmpId(empId);
                                          empHistAggr.add(empHist.getEmployerName());
                                          return empHistAggr;
                                    },
                                    // store in materialied view EMPLOYMENT-HIST-AGGR-MV
                                    Materialized.<Integer, EmploymentHistoryAggregation, KeyValueStore<Bytes, byte[]>>as(
                                                "EMPLOYMENT-HIST-AGGR-MV")
                                                .withKeySerde(Serdes.Integer())
                                                .withValueSerde(employmentHistoryAggregationSerde));

            // b. join with EMP-DEPT. Since the key is already identical, which is EMP_ID,
            // no need FK Extractor
            final KTable<Integer, EmployeeResult> empResultTable = empDeptTable.join(employmentHistoryAggr,
                        // Value Joiner
                        (empResult, histAggr) -> {
                              empResult.setEmploymentHistory(histAggr.getEmploymentHistory());
                              return empResult;
                        },
                        // store in materialied view EMP-RESULT-MV
                        Materialized.<Integer, EmployeeResult, KeyValueStore<Bytes, byte[]>>as("EMP-RESULT-MV")
                                    .withKeySerde(Serdes.Integer())
                                    .withValueSerde(employeeResultSerde));

            // store result to output topic EMP-RESULT
            empResultTable.toStream()
                        .map((key, value) -> new KeyValue<>(value.getEmpId(), value))
                        .peek((key, value) -> System.out.println("(empResultTable) key,value = " + key + "," + value))
                        .to("EMP-RESULT", Produced.with(Serdes.Integer(), employeeResultSerde));

      }

}
