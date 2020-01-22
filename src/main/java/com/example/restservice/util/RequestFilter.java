package com.example.restservice.util;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.MDC;
import org.apache.log4j.Logger;

@Component
public class RequestFilter implements Filter {
    private static Logger logger = Logger.getLogger(RequestFilter.class);
    @Autowired
    LogApiInterceptor logApiInterceptor;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
            // LOG REQUEST
            ResettableStreamHttpServletRequest wrappedRequest = null;
            ResettableStreamHttpServletResponse wrappedResponse = null;
            try {
                    wrappedRequest = new ResettableStreamHttpServletRequest((HttpServletRequest) request);
                    wrappedResponse = new ResettableStreamHttpServletResponse((HttpServletResponse) response);
                    logApiInterceptor.writeRequestPayloadAudit(wrappedRequest);
            } catch (Exception e) {
                    logger.error("Fail to wrap request and response",e);
            }
            try {
                    chain.doFilter(wrappedRequest, wrappedResponse);
            } finally {
                    MDC.clear();
            }
    }
}
