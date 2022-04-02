package com.example.batch.configurations;

import com.example.batch.model.User;
import com.example.batch.pojo.Employee;
import com.example.batch.processor.UserProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;
    @Autowired
    @Qualifier("sourceConnectionSource") DataSource sourceDataSource;
    @Autowired
    @Qualifier("targetConnectionSource") DataSource targetDataSource;


    private static final String QUERY_INSERT_USER = "INSERT_USER";

    private static final String QUERY_FETCH_USER= "FETCH_USER";

//    @Bean
//    @StepScope
//    public MultiResourceItemReader reader(@Value("#{jobParameters['localFiles']}") List<String> paths){
//        MultiResourceItemReader<User> reader= new MultiResourceItemReader<>();
//        reader.setName("multiReader");
//        reader.setDelegate(delegate());
//        List<Resource> resources= paths
//                .stream()
//                .map( FileSystemResource::new)
//                .collect(Collectors.toList());
//        reader.setResources(resources.toArray(new Resource[resources.size()]));
//        return reader;
//    }

    @Bean
    @StepScope
    public FlatFileItemWriter<User> fileWriter(@Value("#{jobExecutionContext['tempPath']}")  String paths,@Value("#{jobParameters['businessDate']}") String fileName) {
        Resource outputResource = new FileSystemResource(paths);
        FlatFileItemWriter<User> writer = new FlatFileItemWriter<>();
        writer.setResource(outputResource);
        writer.setAppendAllowed(false);
        writer.setSaveState(true);
        writer.setHeaderCallback(wtr-> wtr.write("ID|FIRST_NAME|LAST_NAME|EMAIL|GENDER|WORK"));
        writer.setLineAggregator(new DelimitedLineAggregator<>() {
            {
                setDelimiter("|");

                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[] { "id", "firstName", "lastName", "email","gender","work"});
                    }
                });
            }
        });
        return writer;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<User> fileReader(@Value("#{jobExecutionContext['tempPath']}")  String paths, @Value("#{jobParameters['businessDate']}") String fileName)
    {
        FlatFileItemReader<User> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(paths));
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setDelimiter("|");
                        setNames(new String[] {  "id", "firstName", "lastName", "email","gender","work"});
                    }
                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(User.class);
                    }
                });
            }
        });
        return reader;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<User> paginationItemReader(){
        final JdbcPagingItemReader<User> reader = new  JdbcPagingItemReader<>();
        reader.setDataSource(sourceDataSource);
        reader.setFetchSize(10);
        reader.setPageSize(1);
        reader.setRowMapper(this.toUser());
        reader.setQueryProvider(queryProvider());
        return reader;
    }
    public PagingQueryProvider queryProvider(){
        MySqlPagingQueryProvider queryProvider= new MySqlPagingQueryProvider();
        queryProvider.generateFirstPageQuery(1);
        queryProvider.setSelectClause("id, first_name, last_name, email,gender,work");
        queryProvider.setFromClause("from user");
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        return queryProvider;
    }

//    public JdbcCursorItemReader<User> readFromJdbc(){
//        JdbcCursorItemReader<User> itemReader = new JdbcCursorItemReader<>();
//        itemReader.setRowMapper(this.toUser());
//        itemReader.setDataSource(sourceDataSource);
//        itemReader.setSql(QUERY_FETCH_USER);
//        return  itemReader;
//    }

    public RowMapper<User> toUser() {
        return (ResultSet rs, int cnt)-> new User(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4
        ),
        rs.getString(6),
        rs.getString(6));
    }

    @Bean
    @StepScope
    public JsonItemReader delegate(){
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonJsonObjectReader<User> jsonObjectReader = new JacksonJsonObjectReader<>(User.class);
        jsonObjectReader.setMapper(objectMapper);
        return new JsonItemReaderBuilder<User>()
                .jsonObjectReader(jsonObjectReader)
                .name("userReader")
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<User> jdbcWriter(Environment env){
        JdbcBatchItemWriter<User> databaseItemWriter = new JdbcBatchItemWriter<>();
        databaseItemWriter.setDataSource(targetDataSource);
        databaseItemWriter.setJdbcTemplate(new NamedParameterJdbcTemplate(targetDataSource));
        databaseItemWriter.setSql(env.getProperty(QUERY_INSERT_USER));
        databaseItemWriter.setItemPreparedStatementSetter((user,preparedStatement)->{
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setString(2,user.getFirstName());
            preparedStatement.setString(3,user.getLastName());
            preparedStatement.setString(4,user.getEmail());
            preparedStatement.setString(5,user.getGender());
            preparedStatement.setString(6,user.getWork());
        });
        return  databaseItemWriter;
    }

    @Bean
    @Qualifier("stepZero")
    public Step stepZero(){
        return steps.get("stepZero")
                .tasklet(buildTempFile(null,null))
                .build();
    }
    @Bean
    @StepScope
    public Tasklet buildTempFile(@Value("#{jobParameters['businessDate']}") String fileName, Environment env){
        return  new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                ExecutionContext executionContext=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
                executionContext.put("tempPath", env.getProperty("job.file.path")+fileName+".csv");
                log.info("Execution context: {}", executionContext);
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    @Qualifier("stepOne")
    public Step stepOne(PlatformTransactionManager platformTransactionManager){
        return steps.get("stepOne")
                .transactionManager(platformTransactionManager)
                .<User, User>chunk(5)
                .reader(paginationItemReader())
                .processor(new UserProcessor())
                .writer(fileWriter(null,null))
                .build();
    }
    @Bean
    @Qualifier("stepTwo")
    public Step stepTwo(PlatformTransactionManager platformTransactionManager){
        return steps.get("stepTwo")
                .transactionManager(platformTransactionManager)
                .<User, User>chunk(10)
                .reader(fileReader(null,null))
                .processor(getProcessorsecondStep())
                .writer(jdbcWriter(null))
                .build();
    }
    private ItemProcessor<? super User,? extends User> getProcessorsecondStep() {
        return user->{
          //  throw new IllegalArgumentException("Intentionally throwing the exception...");
               return user;
        };
    }
    private ItemProcessor<? super User,? extends User> getProcessor() {
        return user->{
            return user;
        };
    }

    @Bean
    @Qualifier("demoJob")
    public Job demoJob(PlatformTransactionManager platformTransactionManager){
        return jobs.get("demoJob")
                .start(stepZero())
                .next(stepOne(platformTransactionManager))
                .next(stepTwo(platformTransactionManager))
                .build();
    }
}
