package com.example.batch.processor;

import com.example.batch.model.User;
import com.example.batch.pojo.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;



@Slf4j
@Component
public class UserProcessor implements ItemProcessor<User, User> {
    @Override
    public User process(User o) throws Exception {
        log.info(o.toString());
        return o;
    }
}
