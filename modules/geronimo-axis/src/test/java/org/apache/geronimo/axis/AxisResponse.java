package org.apache.geronimo.axis;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.webservices.WebServiceContainer;

public class AxisResponse implements WebServiceContainer.Response {
    private int contentLength;
    private String contentType;
    private String host;
    private OutputStream out;
    private int method;
    private Map parameters;
    private String path;
    private URL uri;
    private int port;
    private Map headers;
    private int statusCode;
    private String statusMessage;

    /**
     * 
     */
    public AxisResponse(String contentType,
        String host,
        String path,
        URL uri,
        int port,
        OutputStream out) {
        this.contentType = contentType;
        this.host = host;
        this.parameters = new HashMap();
        this.path = path;
        this.uri = uri;
        this.port = port;
        this.headers = new HashMap();
        this.out = out;
    }

    public int getContentLength() {
        return contentLength;
    }


    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    public String getHost() {
        return host;
    }

    public OutputStream getOutputStream()  {
        return out;
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

    public URL getURI() {
        return uri;
    }

    /**
     * @return
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return
     */
    public URL getUri() {
        return uri;
    }

    /**
     * @param i
     */
    public void setContentLength(int i) {
        contentLength = i;
    }

    /**
     * @param string
     */
    public void setContentType(String string) {
        contentType = string;
    }

    /**
     * @param string
     */
    public void setHost(String string) {
        host = string;
    }

    /**
     * @param i
     */
    public void setMethod(int i) {
        method = i;
    }

    /**
     * @param map
     */
    public void setParameters(Map map) {
        parameters = map;
    }

    /**
     * @param string
     */
    public void setPath(String string) {
        path = string;
    }

    /**
     * @param i
     */
    public void setPort(int i) {
        port = i;
    }

    /**
     * @param url
     */
    public void setUri(URL url) {
        uri = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int code) {
        statusCode = code; 

    }

    public void setStatusMessage(String responseString) {
        statusMessage = responseString;

    }

    public void setHeader(String name, String value) {
        headers.put(name,value);

    }

}
