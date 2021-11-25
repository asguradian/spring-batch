package com.example.batch.configurations;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "source.db")
@AllArgsConstructor
public class SourceConnection extends  JdbcProperties{

}
