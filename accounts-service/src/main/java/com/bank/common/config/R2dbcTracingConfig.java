package com.bank.common.config;

import io.r2dbc.proxy.ProxyConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
public class R2dbcTracingConfig {

    @Bean
    public static BeanPostProcessor connectionFactoryProxyPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
                if (bean instanceof ConnectionFactory) {
                    return ProxyConnectionFactory.builder((ConnectionFactory) bean).build();
                }
                return bean;
            }
        };
    }
}
