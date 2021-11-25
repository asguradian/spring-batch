package com.example.batch.listeners;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
public class DebugListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext executionContext= stepExecution.getExecutionContext();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        ExecutionContext executionContext= stepExecution.getExecutionContext();
        return ExitStatus.COMPLETED;
    }
}
