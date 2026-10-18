package com.etp.ticketservice.config;

import com.etp.ticketservice.orders.TicketOrderExpiryJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ticket-service's first use of Quartz (replacing the old @Scheduled-based
// TicketOrderExpirySweeper/SchedulingConfiguration) -- JobDetail/Trigger beans here are
// auto-detected and registered by Spring Boot's own QuartzAutoConfiguration, same as
// JpaConfiguration/RabbitMqConfig being dedicated single-purpose @Configuration classes.
@Configuration
public class QuartzConfiguration {

    private static final long SWEEP_INTERVAL_MS = 300_000; // 5 minutes

    @Bean
    public JobDetail ticketOrderExpiryJobDetail() {
        return JobBuilder.newJob(TicketOrderExpiryJob.class)
                .withIdentity("ticketOrderExpiryJob", "orders")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger ticketOrderExpiryTrigger(JobDetail ticketOrderExpiryJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(ticketOrderExpiryJobDetail)
                .withIdentity("ticketOrderExpiryTrigger", "orders")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(SWEEP_INTERVAL_MS)
                        .repeatForever())
                .build();
    }

    @Bean
    public SchedulerFactoryBeanCustomizer autowiringJobFactoryCustomizer(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return schedulerFactoryBean -> schedulerFactoryBean.setJobFactory(jobFactory);
    }
}
