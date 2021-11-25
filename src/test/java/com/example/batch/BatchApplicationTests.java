package com.example.batch;

import com.example.batch.configurations.BatchConfig;
import com.example.batch.configurations.BeansConfig;
import com.example.batch.configurations.SourceConnection;
import com.example.batch.configurations.TargetConnection;
import com.example.batch.model.User;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles(value = "test")
@EnableConfigurationProperties
@TestPropertySource("classpath:queries-test.properties")
@ContextConfiguration(classes = { BatchApplication.class,BeansConfig.class, BatchConfig.class, SourceConnection.class, TargetConnection.class})
class BatchApplicationTests {

	@Autowired
	@Qualifier("sourceConnectionSource")
	private DataSource source;
	@Autowired
	@Qualifier("targetConnectionSource")
	private DataSource target;
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	private static String testPath="src/test/resources/scripts";

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	private void runScripts(List<String> sqlStatement, JdbcTemplate jdbcTemplate){
		sqlStatement.forEach(jdbcTemplate::execute);
	}
	private List<String> buildScripts(String fileName, String ... args) throws  Exception{
		List<String> fileNames= new ArrayList<>();
		fileNames.add(fileName);
		fileNames.addAll(Arrays.asList(args));
		return fileNames.stream()
				.map(fName->testPath+"/"+fName)
				.map( FileSystemResource::new)
				.map(this::readLines)
				.flatMap(lst->lst.stream())
				.collect(Collectors.toList());
	}
	private List<String> readLines(FileSystemResource fileSystemResource){
		try {
			return FileUtils.readLines(fileSystemResource.getFile(),"UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@BeforeEach
	public void setUp() throws Exception {
		runScripts(buildScripts("common.sql","source.sql"), new JdbcTemplate(source));
		runScripts(buildScripts("common.sql","target.sql"), new JdbcTemplate(target));
	}
	@AfterEach
	public void afterEach() throws Exception{
		runScripts(buildScripts("source-cleanup.sql"), new JdbcTemplate(source));
		runScripts(buildScripts("target-cleanup.sql"), new JdbcTemplate(target));
	}

	@Test
	void contextLoads() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("businessDate", "2021-1001-29")
				.toJobParameters();
      JobExecution jobExecution=jobLauncherTestUtils.launchJob(params);
      System.out.println(jobExecution);
      JdbcTemplate targetJdbc= new JdbcTemplate(target);
      List<User> user=targetJdbc.query("select * from user", this.toUser());
      user.forEach(System.out::println);
      Assert.assertTrue(!user.isEmpty());
	}
	private RowMapper<User> toUser() {
		return (ResultSet rs, int cnt)-> new User(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4
		),
				rs.getString(6),
				rs.getString(6));
	}

}
