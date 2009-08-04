/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" 
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.apache.geronimo.console.filter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a HttpServletResponseWrapper to allow us to edit the
 * response content from the filter chain/servlet before committing it to
 * the ServletResponse.
 *
 * @version $Rev$ $Date$
 */
public final class FilterResponseWrapper extends HttpServletResponseWrapper {
	private static final Logger log = LoggerFactory.getLogger(FilterResponseWrapper.class);
    private ByteArrayOutputStream output = null;
    private ResponseOutputStream stream = null;
    private PrintWriter writer = null;

    /**
     * Default constructor which creates a new HttpServletResponseWrapper in
     * place of the default HttpServletResponse, so we can manipulate the
     * stream content before committing as a response to the client.
     * @param response
     */
    public FilterResponseWrapper(HttpServletResponse response) {
        super(response);
        reset();
    }

    /**
     * Gets the current stream content as bytes for easy manipulation.
     * @return
     * @throws IOException
     */
    public byte[] getOutput() throws IOException {
        flushBuffer();
        return output.toByteArray();
    }

    /**
     * Replaces the existing stream content with the updated bytes supplied.
     * @param bytes
     * @throws IOException
     */
    public void setOutput(byte[] bytes) throws IOException {
        reset();
        stream.write(bytes);
    }

    /**
     * Replaces the existing stream content with the updated String supplied.
     * @param s
     * @throws IOException
     */
    public void setOutput(String s) throws IOException {
        setOutput(s.getBytes("UTF-8"));
    }

    /**
     * Write the manipulated stream content out as the ServletResponse
     * to the client.
     * @throws IOException
     */
    public void writeOutput() throws IOException {
        byte[] content = getOutput();
        ServletResponse response = getResponse();
        OutputStream os = response.getOutputStream();
        response.setContentLength(content.length);
        // only write the stream if there is actually something to write
        if (content.length > 0) {
            os.write(content);
        }
        os.close();
    }

    //----- Required method overrides for javax.servlet.ServletResponseWrapper -----

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#flushBuffer()
     */
    @Override
    public void flushBuffer() throws IOException {
        writer.flush();
        stream.flush();
        output.flush();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return stream;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getResponse()
     */
    @Override
    public ServletResponse getResponse() {
        return super.getResponse();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#getWriter()
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#isCommitted()
     */
    @Override
    public boolean isCommitted() {
        return(output.size() > 0);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#reset()
     */
    @Override
    public void reset() {
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
        }
        if (this.stream != null) {
            try {
                this.stream.close();
            }
            catch (IOException e) {
                // ignore
            }
            this.stream = null;
        }
        if (this.output != null) {
            try {
                this.output.close();
            }
            catch (IOException e) {
                // ignore
            }
            this.output = null;
        }
        this.output = new ByteArrayOutputStream();
        this.stream = new ResponseOutputStream(output);
        try{
        	this.writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, "UTF-8")));
        }
        catch (UnsupportedEncodingException uee) {
            // should never happen
            log.error("new OutputStreamWriter(stream, UTF-8) failed.", uee);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletResponseWrapper#resetBuffer()
     */
    @Override
    public void resetBuffer() {
        reset();
    }

}
