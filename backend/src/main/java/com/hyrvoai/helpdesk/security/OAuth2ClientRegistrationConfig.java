package com.hyrvoai.helpdesk.security;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OAuth2ClientRegistrationConfig {

    @Bean
    public static BeanPostProcessor oauth2ClientRegistrationPostProcessor() {

        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(
                    Object bean,
                    String beanName
            ) throws BeansException {

                if (!(bean instanceof InMemoryClientRegistrationRepository repository)) {
                    return bean;
                }

                List<ClientRegistration> registrations = new ArrayList<>();

                repository.forEach(registration -> {

                    if ("linkedin".equals(registration.getRegistrationId())) {

                        ClientRegistration updated =
                                ClientRegistration
                                        .withClientRegistration(registration)
                                        .clientSettings(
                                                ClientRegistration.ClientSettings
                                                        .builder()
                                                        .requireProofKey(false)
                                                        .build()
                                        )
                                        .build();

                        registrations.add(updated);

                    } else {
                        registrations.add(registration);
                    }
                });

                return new InMemoryClientRegistrationRepository(registrations);
            }
        };
    }
}