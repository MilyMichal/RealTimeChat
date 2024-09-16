package com.m.m.RealTimeChat.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {

       // String jdbcUrl = databaseUrl.replace("postgres://","jdbc:postgresql://");

        URI uri = new URI(databaseUrl);
    String username = uri.getUserInfo().split(":")[0];
    String password = uri.getUserInfo().split(":",2)[1];
    String url = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath() + "?sslmode=require";

        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        return dataSourceBuilder.build();

    } else {
            throw new URISyntaxException(databaseUrl, "Neplatný formát DATABASE_URL");
        }
}
}
