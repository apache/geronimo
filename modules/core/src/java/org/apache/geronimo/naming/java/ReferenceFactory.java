package org.apache.geronimo.naming.java;

import javax.naming.NamingException;
import javax.naming.Reference;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/12 08:18:21 $
 *
 * */
public interface ReferenceFactory {

    Reference getReference(String link, Object locator) throws NamingException;
}
