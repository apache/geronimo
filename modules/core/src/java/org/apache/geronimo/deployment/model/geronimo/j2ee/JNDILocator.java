package org.apache.geronimo.deployment.model.geronimo.j2ee;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/13 22:22:30 $
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
