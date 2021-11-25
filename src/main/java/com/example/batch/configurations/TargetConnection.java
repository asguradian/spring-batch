package com.example.batch.configurations;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "target.db")
public class TargetConnection extends  JdbcProperties{

}
