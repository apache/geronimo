package org.apache.geronimo.naming.java;

import javax.naming.Reference;
import javax.naming.NamingException;

import org.apache.geronimo.deployment.model.geronimo.j2ee.JNDILocator;

/**
 * 
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/13 22:22:30 $
 * 
 * */
public interface ReferenceFactory {

    Reference getReference(JNDILocator locator, String type) throws NamingException;
}
