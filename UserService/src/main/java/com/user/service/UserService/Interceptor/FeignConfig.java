package com.user.service.UserService.Interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private static final String INTERNAL_KEY_HEADER = "X-INTERNAL-KEY";
    private static final String SECRET_KEY = "MY_SECRET_KEY";

    @Bean
    public RequestInterceptor internalKeyInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header(INTERNAL_KEY_HEADER, SECRET_KEY);
            }
        };
    }
}
