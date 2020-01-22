package com.example.restservice.util;


import javax.servlet.http.HttpServletResponseWrapper;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

public class ResettableStreamHttpServletResponse extends HttpServletResponseWrapper {

        public String requestId;
        public String payloadFilePrefix;
        public String payloadTarget;

        public List<Byte> rawData = new ArrayList<Byte>();
        public HttpServletResponse response;
        private ResettableServletOutputStream servletStream;

        ResettableStreamHttpServletResponse(HttpServletResponse response) throws IOException {
                super(response);
                this.response = response;
                this.servletStream = new ResettableServletOutputStream(this);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
                return servletStream;
        }
        public PrintWriter getWriter() throws IOException {
                String encoding = getCharacterEncoding();
                if ( encoding != null ) {
                        return new PrintWriter(new OutputStreamWriter(servletStream, encoding));
                } else {
                        return new PrintWriter(new OutputStreamWriter(servletStream));
                }
        }
}
