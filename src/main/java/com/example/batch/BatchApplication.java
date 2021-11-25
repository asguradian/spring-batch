package com.example.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@SpringBootApplication
@EnableBatchProcessing
@PropertySource("classpath:queries.properties")
@Slf4j
public class BatchApplication implements CommandLineRunner {
	@Autowired
	JobLauncher jobLauncher;
	@Autowired
	JobExplorer jobExplorer;
	@Autowired
	JobRegistry jobRegistry;
	@Autowired
	Job job;


	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}
	@Override
	public void run(String... args) throws Exception {
       log.info("Starting the job...");
        JobParameters params = new JobParametersBuilder()
				.addString("businessDate", "2023-2002-29")
				//.addString("tmpPath","/Users/anilacharya/Downloads/tmp.csv")
				.toJobParameters();

	jobLauncher.run(job, params);
 	System.out.println("Process completed!!!!!!");
	}


	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
		JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
		postProcessor.setJobRegistry(jobRegistry);
		return postProcessor;
	}
}
