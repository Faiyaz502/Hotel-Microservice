package com.hotel.service.HotelService.multipleDbManagement;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class DataSourceConfig {

    // --- 1. Define individual DataSources using the YAML keys ---

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica1")
    public DataSource replica1DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica2")
    public DataSource replica2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica3")
    public DataSource replica3DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    // --- 2. The Routing Logic (Switching between Primary and Replicas) ---

    @Bean
    @Primary
    public DataSource dataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("WRITE", primaryDataSource());

        // Wrap the 3 Replicas in a Load Balancer Proxy
        targetDataSources.put("READ", replicaLoadBalancer());

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource());
        return routingDataSource;
    }

    // --- 3. Round-Robin Load Balancer for the 3 Replicas ---

    private DataSource replicaLoadBalancer() {
        List<DataSource> replicas = List.of(
                replica1DataSource(),
                replica2DataSource(),
                replica3DataSource()
        );

        AtomicInteger counter = new AtomicInteger(0);

        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class[]{DataSource.class},
                (proxy, method, args) -> {
                    // Only load balance if calling a method that needs a connection
                    int index = Math.abs(counter.getAndIncrement() % replicas.size());
                    return method.invoke(replicas.get(index), args);
                }
        );
    }
}
