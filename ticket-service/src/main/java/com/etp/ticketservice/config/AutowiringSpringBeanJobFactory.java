package com.etp.ticketservice.config;

import org.jspecify.annotations.NonNull;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

// Quartz builds each Job instance itself via reflection on every fire, one job class
// instantiation per trigger -- it never asks the Spring container for the @Component-managed
// singleton. Without this, @Autowired fields on a Job (e.g. TicketOrderExpiryJob) stay null.
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    private transient AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    protected @NonNull Object createJobInstance(@NonNull TriggerFiredBundle bundle) throws Exception {
        Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
}
