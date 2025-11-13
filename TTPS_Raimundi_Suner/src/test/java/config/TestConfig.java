package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"models.hibernate"})
public class TestConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPersistenceUnitName("test-pu");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setPackagesToScan("models.clases");
        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(emf.getObject());
        return tm;
    }
}

