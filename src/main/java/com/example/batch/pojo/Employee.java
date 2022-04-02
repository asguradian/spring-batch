package com.example.batch.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class Employee {

    private String firstName;
    private String lastName;
    private int age;
    private String address;

}
