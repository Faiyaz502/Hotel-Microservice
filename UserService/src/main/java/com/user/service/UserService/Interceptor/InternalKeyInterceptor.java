package com.user.service.UserService.Interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class InternalKeyInterceptor implements ClientHttpRequestInterceptor {

    private static final String INTERNAL_KEY_HEADER = "X-INTERNAL-KEY";
    private static final String SECRET_KEY = "MY_SECRET_KEY";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Add the internal key header
        request.getHeaders().add(INTERNAL_KEY_HEADER, SECRET_KEY);

        // Continue with the request
        return execution.execute(request, body);
    }
}