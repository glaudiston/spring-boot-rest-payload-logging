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
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import org.apache.log4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LogApiInterceptor extends HandlerInterceptorAdapter {
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
    public void writeRequestPayloadAudit(ResettableStreamHttpServletRequest wrappedRequest) throws Exception {
	    MDC.put("uid", UUID.randomUUID());
	    String requestHeaders = getRawHeaders(wrappedRequest);
	    String requestBody = org.apache.commons.io.IOUtils.toString(wrappedRequest.getReader());
	    System.out.println("writeRequestPayloadAudit - request id  " + MDC.get("uid"));
	    System.out.println("Request Method: "+wrappedRequest.getMethod());
	    System.out.println("Request Headers:");
	    System.out.println(requestHeaders);
	    System.out.println("Request body:");
	    System.out.println(requestBody);
    }
    public void writeResponsePayloadAudit(ResettableStreamHttpServletResponse wrappedResponse){
	    String payloadFile = "/tmp/" + MDC.get("uid") + "-request.txt";
            String rawHeaders = getRawHeaders(wrappedResponse);
	    System.out.println("writeResponsePayloadAudit - request id " + MDC.get("uid"));
	    System.out.println("Response Status: " + wrappedResponse.getStatus());
	    System.out.println("Response Headers:");
	    System.out.println(rawHeaders);
	    System.out.println("Response body:");
	    byte[] data = new byte[wrappedResponse.rawData.size()];
	    for (int i = 0; i < data.length; i++) {
		        data[i] = (byte) wrappedResponse.rawData.get(i);
	    }
	    String responseBody = new String(data);
	    System.out.println(responseBody);
	    // String requestBody = org.apache.commons.io.IOUtils.toString(obj.getReader());
	    // writePayloadAudit(payloadFile, rawHeaders, requestBody);
    }
}
