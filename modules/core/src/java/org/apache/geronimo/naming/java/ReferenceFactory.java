package org.apache.geronimo.naming.java;

import javax.naming.Reference;
import javax.naming.NamingException;

import org.apache.geronimo.deployment.model.geronimo.j2ee.JNDILocator;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/11/16 05:24:38 $
 *
 * */
public interface ReferenceFactory {

    Reference getReference(String link, JNDILocator locator) throws NamingException;
}
