package org.apache.geronimo.security.bridge;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

/**
 * Interface for bridging between realms.  Subject from a source realm is supplied, and
 * the RealmBridge logs into a target realm using identity and credential information from
 * source realm, mapped as appropriate.
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/11 08:27:02 $
 *
 * */
public interface RealmBridge {

    Subject mapSubject(Subject sourceSubject) throws LoginException;
}
