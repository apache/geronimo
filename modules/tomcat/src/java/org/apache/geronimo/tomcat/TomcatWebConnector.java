package org.apache.geronimo.tomcat;

import org.apache.geronimo.management.geronimo.WebConnector;

/**
 * Tomcat-specific connector properties.  For full documentation on all the
 * available properties (not all are exposed yet), see
 * http://jakarta.apache.org/tomcat/tomcat-5.5-doc/config/http.html
 *
 * @version $Revision: 1.0$
 */
public interface TomcatWebConnector extends WebConnector {
    public int getMinSpareThreads();
    public void setMinSpareThreads(int threads);
    public int getMaxSpareThreads();
    public void setMaxSpareThreads(int threads);
    public int getMaxHttpHeaderSizeBytes();
    public void setMaxHttpHeaderSizeBytes(int bytes);
    public boolean isHostLookupEnabled();
    public void setHostLookupEnabled(boolean enabled);
    public int getConnectionTimeoutMillis();
    public void setConnectionTimeoutMillis(int millis);
    public boolean isUploadTimeoutEnabled();
    public void setUploadTimeoutEnabled(boolean enabled);
}
