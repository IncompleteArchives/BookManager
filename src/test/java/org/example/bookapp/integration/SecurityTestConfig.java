package org.example.bookapp.integration;

import org.example.bookapp.config.JpaConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@ComponentScan({
        "org.example.bookapp.service",
        "org.example.bookapp.repository",
        "org.example.bookapp.security"
})
@Import(JpaConfig.class)
public class SecurityTestConfig {

    @Bean
    public DataSource dataSource() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setUrl(PostgresTestConfig.dbUrl);
        dataSource.setUsername(PostgresTestConfig.dbUsername);
        dataSource.setPassword(PostgresTestConfig.dbPassword);
        dataSource.setDriverClassName(PostgresTestConfig.dbDriver);

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
