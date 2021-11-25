package com.example.batch.configurations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class JdbcProperties {
    protected String url;
    protected String userName;
    protected String password;
    protected String driverName;
}
