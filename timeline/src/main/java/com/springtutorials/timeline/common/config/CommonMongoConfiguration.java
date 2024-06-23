package com.springtutorials.timeline.common.config;

import com.mongodb.MongoClientSettings;
import lombok.RequiredArgsConstructor;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.MongoPropertiesClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
@RequiredArgsConstructor
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.springtutorials.timeline.common.repository.mongo")
public class CommonMongoConfiguration extends AbstractMongoClientConfiguration {
    private final MongoProperties mongoProperties;
    private final MongoPropertiesClientSettingsBuilderCustomizer customizer;

    @Bean
    MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getDatabase();
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.codecRegistry(fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build())));
        customizer.customize(builder);
    }

    @Configuration
    @ConditionalOnMissingBean(MongoPropertiesClientSettingsBuilderCustomizer.class)
    static class CustomizerConfig {

        @Bean
        MongoPropertiesClientSettingsBuilderCustomizer mongoPropertiesCustomizer(MongoProperties mongoProperties,
                                                                                 Environment env) {
            return new MongoPropertiesClientSettingsBuilderCustomizer(mongoProperties, env);
        }
    }
}
