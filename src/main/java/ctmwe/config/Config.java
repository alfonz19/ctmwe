package ctmwe.config;

import ctmwe.repositories.jpa.main.entities.SampleTableEntity;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SuppressWarnings("squid:S1118") //just because there are no instance members, does not mean, that class need not to be instantiated. Definitely not MAJOR code smell.
@Configuration
@PropertySource("classpath:application.properties")
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = {SampleTableEntity.class})
public class Config {
    public static final String ENTITY_SCHEMA = "aaa";


}
