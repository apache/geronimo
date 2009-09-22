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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A response wrapper that will replace certain keyword (</body>) with a given
 * string in the output.
 * @version $Rev$, $Date$
 */
public class SubstituteResponseWrapper extends HttpServletResponseWrapper {
    private static final Logger log = LoggerFactory.getLogger(SubstituteResponseWrapper.class);

    private SubstituteResponseOutputStream stream = null;
    private SubstitutePrintWriter writer = null;
    private String substitute = null;

    public SubstituteResponseWrapper(HttpServletResponse response,
            String substitute) {
        super(response);
        this.substitute = substitute;
    }

    private boolean substituteRequired() {
        String cType = getContentType();
        if (cType != null) {
            // only update the content if it is HTML
            if (cType.toLowerCase().indexOf("html") != -1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (substituteRequired()) {
            if (writer != null) {
                writer.flush();
            } else if (stream != null) {
                stream.flush();
            }
        } else {
            super.flushBuffer();
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        log.debug("Getting a stream...");
        if (!substituteRequired()) {
            return super.getOutputStream();
        } else if (writer != null) {
            throw new IllegalStateException(
                    "getWriter() has already been called on this response.");
        } else if (stream == null) {
            stream = new SubstituteResponseOutputStream(substitute,
                    getCharacterEncoding(), super.getOutputStream());
        }
        return stream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        log.debug("Getting a writer...");
        if (!substituteRequired()) {
            return super.getWriter();
        } else if (stream != null) {
            throw new IllegalStateException(
                    "getStream() has already been called on this response.");
        } else if (writer == null) {
            writer = new SubstitutePrintWriter(new SubstituteWriter(
                    substitute, getCharacterEncoding(), super
                            .getOutputStream()));
        }
        return writer;

    }

    @Override
    public void reset() {
        log.debug("Resetting...");
        super.reset();
        // If no exception from the wrapped response, let's reset too
        if (substituteRequired()) {
            if (stream != null) {
                stream.reset();
            } else if (writer != null) {
                writer.reset();
            }
        }
    }

    @Override
    public void resetBuffer() {
        log.debug("Resetting buffer...");
        super.resetBuffer();
        if (substituteRequired()) {
            // If no exception from the wrapped response, let's reset too
            if (stream != null) {
                stream.reset();
            } else if (writer != null) {
                writer.reset();
            }
        }
    }
}
