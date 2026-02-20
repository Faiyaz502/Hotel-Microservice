package com.service.ratingService.RatingService.Security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class InternalKeyFilter implements Filter {

    private static final String INTERNAL_KEY_HEADER = "X-INTERNAL-KEY";
    private static final String SECRET_KEY = "MY_SECRET_KEY";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String key = httpRequest.getHeader(INTERNAL_KEY_HEADER);

        if (!SECRET_KEY.equals(key)) {
            throw new ServletException("External calls not allowed");
        }

        chain.doFilter(request, response);
    }
}