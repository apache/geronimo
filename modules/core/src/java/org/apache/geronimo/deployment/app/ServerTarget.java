package org.apache.geronimo.deployment.app;

import java.io.Serializable;
import javax.enterprise.deploy.spi.Target;

/**
 * A target representing a single (non-clustered) Geronimo server.
 *
 * @version $Revision: 1.1 $
 */
public class ServerTarget implements Target, Serializable {
    private String hostname;

    public ServerTarget(String hostname) {
        this.hostname = hostname;
    }

    public String getName() {
        return hostname;
    }

    public String getDescription() {
        return "Geronimo Server on "+hostname;
    }
}
