package org.apache.geronimo.security.bridge;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;


/**
 * Interface for bridging between realms.  Subject from a source realm is supplied, and
 * the RealmBridge logs into a target realm using identity and credential information from
 * source realm, mapped as appropriate.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/17 00:05:39 $
 */
public interface RealmBridge {

    Subject mapSubject(Subject sourceSubject) throws LoginException;
}
