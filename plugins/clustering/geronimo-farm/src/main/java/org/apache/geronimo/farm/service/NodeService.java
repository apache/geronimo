/**
 * 
 */
package org.apache.geronimo.farm.service;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

public class NodeService implements Serializable {

    private static final long serialVersionUID = 8329271824511964537L;
    private final URI uri;
    private final String uriString;

    public URI getUri() {
        return uri;
    }

    public String getUriString() {
        return uriString;
    }

    public NodeService(URI uri) {
        this.uri = uri;
        this.uriString = uri.toString();
    }

    public NodeService(String uriString) throws URISyntaxException {
        this(new URI(uriString));
    }
}
