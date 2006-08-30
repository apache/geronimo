package org.apache.geronimo.axis;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import org.apache.geronimo.webservices.WebServiceContainer;

public class AxisRequest implements WebServiceContainer.Request {
    private int contentLength;
    private String contentType;
    private InputStream in;
    private int method;
    private Map parameters;
    private URI uri;
    private Map headers;
    private Map attributes;

    /**
     * 
     */
    public AxisRequest(
        int contentLength,
        String contentType,
        InputStream in,
        int method,
        Map parameters,
        URI uri,
        Map headers) {
        this.contentType = contentType;
        this.in = in;
        this.method = method;
        this.parameters = parameters;
        this.uri = uri;
        this.headers = headers;
        this.attributes = new HashMap();
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    public InputStream getInputStream() throws IOException {
        return in;
    }

    public int getMethod() {
        return method;
    }

    public String getParameter(String name) {
        return (String) parameters.get(name);
    }

    public Map getParameters() {
        return parameters;
    }

    public URI getURI() {
        return uri;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value){
        attributes.put(name, value);
    }
}
