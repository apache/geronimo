package org.apache.geronimo.naming.deployment;

import org.apache.xmlbeans.XmlObject;

/**
 * adapting wrapper for different xml objects generated for different schema incusions.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/09 18:03:11 $
 *
 * */
public interface RefAdapter {
    XmlObject getXmlObject();
    void setXmlObject(XmlObject xmlObject);

    String getRefName();
    void setRefName(String name);
    String getServerName();
    void setServerName(String serverName);
    String getKernelName();
    void setKernelName(String kernelName);
    String getTargetName();
    void setTargetName(String targetName);
    String getExternalUri();
    void setExternalUri(String externalURI);
}
