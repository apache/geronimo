package org.apache.geronimo.naming.java;

import javax.naming.Reference;
import javax.naming.NamingException;

import org.apache.geronimo.deployment.model.geronimo.j2ee.JNDILocator;

/**
 * 
 *
 * @version $VERSION$ Nov 12, 2003$
 * 
 * */
public interface ReferenceFactory {

    Reference getReference(JNDILocator locator, String type) throws NamingException;
}
