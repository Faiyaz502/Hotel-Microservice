package com.hotel.service.HotelService.multipleDbManagement;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0)       //---- Must run BEFORE @Transactional
public class DataSourceAspect {

    @Around("@annotation(com.hotel.service.HotelService.multipleDbManagement.ReadOnly)")
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            // Switch to the 3 Replicas
            DataSourceContextHolder.set("READ");
            return proceedingJoinPoint.proceed();
        } finally {
            // Always clear to prevent memory leaks and defaulting to Primary
            DataSourceContextHolder.clear();
        }
    }
}