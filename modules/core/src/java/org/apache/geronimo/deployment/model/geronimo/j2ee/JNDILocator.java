package org.apache.geronimo.deployment.model.geronimo.j2ee;

/**
 * 
 *
 * @version $VERSION$ Nov 12, 2003$
 * 
 * */
public interface JNDILocator {
    JndiContextParam[] getJndiContextParam();

    JndiContextParam getJndiContextParam(int i);

    void setJndiContextParam(JndiContextParam[] jndiContextParam);

    void setJndiContextParam(int i, JndiContextParam jndiContextParam);

    String getJndiName();

    void setJndiName(String jndiName);
}
