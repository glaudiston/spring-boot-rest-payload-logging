package com.example.restservice.util;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.lang.StringBuffer;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.Collections;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.apache.log4j.Logger;
import java.util.UUID;

@Component
public class LogApiInterceptor extends HandlerInterceptorAdapter {
	private static final Logger LOGGER = Logger.getLogger(LogApiInterceptor.class);
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            if ( response instanceof ResettableStreamHttpServletResponse ) {
                    ((ResettableStreamHttpServletResponse)response).payloadFilePrefix = ((ResettableStreamHttpServletRequest)request).payloadFilePrefix;
                    ((ResettableStreamHttpServletResponse)response).payloadTarget  = ((ResettableStreamHttpServletRequest)request).payloadTarget;
                    writeResponsePayloadAudit((ResettableStreamHttpServletResponse) response);
            }
    }
    public String getRawHeaders(HttpServletRequest request) {
            StringBuffer rawHeaders = new StringBuffer();
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                    String key = (String) headerNames.nextElement();
                    String value = request.getHeader(key);
                    rawHeaders.append(key).append(":").append(value).append("\n");
            }

            return rawHeaders.toString();
    }
    public String getRawHeaders(HttpServletResponse response){
            StringBuffer rawHeaders = new StringBuffer();
            Enumeration headerNames = Collections.enumeration(response.getHeaderNames());
            while (headerNames.hasMoreElements()) {
                    String key = (String) headerNames.nextElement();
                    String value = response.getHeader(key);
                    rawHeaders.append(key).append(":").append(value).append("\n");
            }

            return rawHeaders.toString();
    }
    private void writePayloadAudit(String payloadFile, String rawHeaders, String requestBody) throws IOException {
            try (Writer writer = new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(payloadFile), StandardCharsets.UTF_8))) {
                    writer.write(rawHeaders);
                    writer.write("\n");
                    writer.write(requestBody);
            }
    }
    public void writeRequestPayloadAudit(ResettableStreamHttpServletRequest wrappedRequest) {
	    try {
		    String requestHeaders = getRawHeaders(wrappedRequest);
		    String requestBody = org.apache.commons.io.IOUtils.toString(wrappedRequest.getReader());
		    LOGGER.info("Request Method: "+wrappedRequest.getMethod());
		    LOGGER.info("Request Headers:");
		    LOGGER.info(requestHeaders);
		    LOGGER.info("Request body:");
		    LOGGER.info(requestBody);
	    } catch (Exception e) {
		    LOGGER.error(e);
	    }
    }
    public void writeResponsePayloadAudit(ResettableStreamHttpServletResponse wrappedResponse){
            String rawHeaders = getRawHeaders(wrappedResponse);
	    LOGGER.info("Response Status: " + wrappedResponse.getStatus());
	    LOGGER.info("Response Headers:");
	    LOGGER.info(rawHeaders);
	    LOGGER.info("Response body:");
	    byte[] data = new byte[wrappedResponse.rawData.size()];
	    for (int i = 0; i < data.length; i++) {
		data[i] = (byte) wrappedResponse.rawData.get(i);
	    }
	    String responseBody = new String(data);
	    LOGGER.info(responseBody);
	    // String requestBody = org.apache.commons.io.IOUtils.toString(obj.getReader());
	    // writePayloadAudit(payloadFile, rawHeaders, requestBody);
    }
}
