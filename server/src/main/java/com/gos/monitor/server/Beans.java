package com.gos.monitor.server;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.sql.DataSource;

/**
 * Created by xue on 2017-05-19.
 */
@Service
public class Beans {

    @Bean
    public JdbcTemplate getJdbcTemplate() {
        DataSource dataSource = this.getDataResource();
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DataSource getDataResource() {
        PoolConfiguration conf = new PoolProperties();
        conf.setUrl("mysql:jdbc://127.0.0.1:3306/gos-monitor");
        conf.setUsername("root");
        conf.setPassword("root");
        conf.setMaxActive(256);
        conf.setMaxAge(1000 * 60 * 10);
        DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource(conf);
        return dataSource;
    }

    @Service
    public static class PageResource extends WebMvcConfigurerAdapter {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**").addResourceLocations("classpath:pages/");
        }
    }
}
