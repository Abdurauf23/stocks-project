package com.stocks.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:queries.properties")
public class PropertiesWithJavaConfig {}
