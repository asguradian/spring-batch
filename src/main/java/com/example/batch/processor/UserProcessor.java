package com.example.batch.processor;

import com.example.batch.model.User;
import com.example.batch.pojo.Employee;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;



@Component
public class UserProcessor implements ItemProcessor<User, User> {
    @Override
    public User process(User o) throws Exception {
        return o;
    }
}
