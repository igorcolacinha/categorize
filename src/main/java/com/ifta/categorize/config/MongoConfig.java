package com.ifta.categorize.config;

import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

@EnableMongoRepositories(basePackages = {"com.ifta.categorize.repository"})
public final class MongoConfig extends AbstractMongoClientConfiguration {

    private final List<Converter<?, ?>> converters = new ArrayList<>();

    @Override
    protected String getDatabaseName() {
        return "categorize";
    }

    @Override
    public MongoCustomConversions customConversions() {
        converters.add(new ZonedDateTimeWriteConverter());
        converters.add(new ZonedDateTimeReadConverter());
        return new MongoCustomConversions(converters);
    }
}
