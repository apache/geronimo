package org.apache.geronimo.axis;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.apache.geronimo.webservices.WebServiceContainer;

public class AxisRequest implements WebServiceContainer.Request {
    private int contentLength;
    private String contentType;
    private String host;
    private InputStream in;
    private int method;
    private Map parameters;
    private String path;
    private URI uri;
    private int port;
    private Map headers;

    /**
     * 
     */
    public AxisRequest(
        int contentLength,
        String contentType,
        String host,
        InputStream in,
        int method,
        Map parameters,
        String path,
        URI uri,
        int port,
        Map headers) {
        this.contentType = contentType;
        this.host = host;
        this.in = in;
        this.method = method;
        this.parameters = parameters;
        this.path = path;
        this.uri = uri;
        this.port = port;
        this.headers = headers;
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

    public String getHost() {
        return host;
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

    public String getPath() {
        return path;
    }

    public int getPort() {
        return port;
    }

    public URI getURI() {
        return uri;
    }

}
