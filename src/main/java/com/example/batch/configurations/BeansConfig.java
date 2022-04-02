package com.example.batch.configurations;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;

@Configuration
public class BeansConfig {

    @Bean
    BatchConfigurer configurer( Environment env){
        return new DefaultBatchConfigurer(getBatchDataSource(env));
    }

    @Primary
    @Bean
    @Qualifier("default")
    public DataSource getBatchDataSource(Environment env){
        SourceConnection sourceConnection = new SourceConnection();
        sourceConnection.setDriverName(env.getProperty("spring.datasource.driverName"));
        sourceConnection.setUserName(env.getProperty("spring.datasource.username"));
        sourceConnection.setPassword(env.getProperty("spring.datasource.password"));
        sourceConnection.setUrl(env.getProperty("spring.datasource.url"));
        return this.getConnection( sourceConnection);
    }



    private DataSource getConnection(JdbcProperties jdbcProperties){
       return  DataSourceBuilder.create().url(jdbcProperties.getUrl())
                .username(jdbcProperties.getUserName())
                .password(jdbcProperties.getPassword())
                .driverClassName(jdbcProperties.getDriverName())
                .build();
    }
    @Bean
    @Qualifier("sourceConnectionSource")
    public DataSource source(SourceConnection sourceConnection){
        return this.getConnection(sourceConnection);
    }
     @Bean("targetConnectionSource")
    public DataSource target( TargetConnection targetConnection, SourceConnection sourceConnection){
        return this.getConnection(targetConnection) ;
    }

}
